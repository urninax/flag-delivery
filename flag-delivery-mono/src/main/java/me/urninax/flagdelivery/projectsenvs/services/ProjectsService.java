package me.urninax.flagdelivery.projectsenvs.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import me.urninax.flagdelivery.projectsenvs.models.environment.Environment;
import me.urninax.flagdelivery.projectsenvs.models.project.CasingConvention;
import me.urninax.flagdelivery.projectsenvs.models.project.Project;
import me.urninax.flagdelivery.projectsenvs.models.project.ProjectTag;
import me.urninax.flagdelivery.projectsenvs.models.project.ProjectTagId;
import me.urninax.flagdelivery.projectsenvs.repositories.ProjectsRepository;
import me.urninax.flagdelivery.projectsenvs.shared.CommonSpecifications;
import me.urninax.flagdelivery.projectsenvs.shared.project.ProjectDTO;
import me.urninax.flagdelivery.projectsenvs.shared.project.ProjectSpecifications;
import me.urninax.flagdelivery.projectsenvs.ui.models.requests.project.CreateProjectRequest;
import me.urninax.flagdelivery.projectsenvs.ui.models.requests.project.ListAllProjectsRequest;
import me.urninax.flagdelivery.projectsenvs.ui.models.requests.project.NamingConventionRequest;
import me.urninax.flagdelivery.projectsenvs.ui.models.requests.project.PatchProjectRequest;
import me.urninax.flagdelivery.projectsenvs.utils.exceptions.project.InvalidPrefixException;
import me.urninax.flagdelivery.projectsenvs.utils.exceptions.project.ProjectAlreadyExistsException;
import me.urninax.flagdelivery.projectsenvs.utils.exceptions.project.ProjectNotFoundException;
import me.urninax.flagdelivery.projectsenvs.utils.exceptions.project.SortNotPossibleException;
import me.urninax.flagdelivery.projectsenvs.utils.exceptions.environment.EnvironmentAlreadyExistsException;
import me.urninax.flagdelivery.shared.security.CurrentUser;
import me.urninax.flagdelivery.shared.utils.EntityMapper;
import me.urninax.flagdelivery.shared.utils.PersistenceExceptionUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectsService{
    private final ProjectsRepository projectsRepository;
    private final EnvironmentsService environmentsService;
    private final EntityMapper entityMapper;
    private final CurrentUser currentUser;
    private final Clock clock;

    @Transactional
    public ProjectDTO createProject(CreateProjectRequest request){
        UUID userId = currentUser.getUserId();
        UUID organisationId = currentUser.getOrganisationId();

        Project project = Project.builder()
                .name(request.name().trim().replaceAll("\\s+", " "))
                .key(request.key())
                .organisationId(organisationId)
                .casingConvention(CasingConvention.NONE)
                .createdBy(userId)
                .createdAt(Instant.now(clock))
                .updatedAt(Instant.now(clock))
                .build();

        if(request.namingConvention() != null){
            CasingConvention convention = request.namingConvention().casingConvention();
            String prefix = request.namingConvention().prefix();

            if(convention != null){
                project.setCasingConvention(convention);

                if(prefix != null){
                    validatePrefixAgainstConvention(convention, prefix);
                    project.setPrefix(prefix);
                }
            }
        }

        Set<ProjectTag> tags = request.tags()
                .stream()
                .map(tag -> new ProjectTag(
                        new ProjectTagId(null, tag), project))
                .collect(Collectors.toSet());

        project.setTags(tags);

        List<Environment> envsList;

        if(request.environments() == null || request.environments().isEmpty()){
            envsList = environmentsService.generateDefaultEnvironments(project);
        }else{
            envsList = request.environments()
                    .stream()
                    .map(env -> environmentsService.buildEnvironment(env, project))
                    .toList();
        }
        Set<Environment> envsSet = new HashSet<>(envsList);

        if(envsSet.size() < envsList.size()){
            throw new EnvironmentAlreadyExistsException();
        }

        project.setEnvironments(envsSet);

        try{
            Project createdProject = projectsRepository.saveAndFlush(project);
            return entityMapper.toDTO(createdProject);
        }catch(DataIntegrityViolationException exc){
            if(PersistenceExceptionUtils.isUniqueException(exc)){
                throw new ProjectAlreadyExistsException();
            }
            throw exc;
        }
    }

    public ProjectDTO getProject(String projectKey, String expand){
        Project project = projectsRepository.findByOrganisationIdAndKey(currentUser.getOrganisationId(), projectKey)
                .orElseThrow(ProjectNotFoundException::new);

        return "environments".equals(expand) ? entityMapper.toExpandedDTO(project) : entityMapper.toDTO(project);
    }

    public Page<ProjectDTO> getPaginatedProjects(ListAllProjectsRequest request, Pageable pageable){
        Specification<Project> projectSpec = ProjectSpecifications.byOrganisation(currentUser.getOrganisationId());

        if(request != null){
            if(!request.query().isBlank()){
                projectSpec = projectSpec.and(CommonSpecifications.hasQuery(request.query()));
            }

            if(!request.keys().isEmpty()){
                projectSpec = projectSpec.and(ProjectSpecifications.hasAnyKeyLike(request.keys()));
            }

            if(!request.tags().isEmpty()){
                projectSpec = projectSpec.and(ProjectSpecifications.hasAllTags(request.tags()));
            }
        }

        Pageable sanitizedPageable = sanitize(pageable);

        Page<Project> projectPage = projectsRepository.findAll(projectSpec, sanitizedPageable);

        return projectPage.map(entityMapper::toDTO);
    }

    @Transactional
    public void patchProject(String projectKey, PatchProjectRequest request){
        if(request.name() == null && request.tags() == null){
            return;
        }

        Project project = projectsRepository.findByOrganisationIdAndKey(currentUser.getOrganisationId(), projectKey)
                .orElseThrow(ProjectNotFoundException::new);

        if(request.name() != null && !request.name().isBlank()){
            project.setName(request.name().trim().replaceAll("\\s+", " "));
        }

        if(request.tags() != null){
            Set<ProjectTag> tags = request.tags()
                    .stream()
                    .map(tag -> new ProjectTag(
                            new ProjectTagId(null, tag), project))
                    .collect(Collectors.toSet());

            project.getTags().clear();
            project.getTags().addAll(tags);
        }

        projectsRepository.save(project);
        //todo: return dto (saveAndFlush)
    }

    @Transactional
    public void editProjectFlagsSettings(String projectKey, NamingConventionRequest request){
        if(request.casingConvention() == null && request.prefix() == null){
            return;
        }

        Project project = projectsRepository.findByOrganisationIdAndKey(currentUser.getOrganisationId(), projectKey)
                .orElseThrow(ProjectNotFoundException::new);

        CasingConvention convention = request.casingConvention() != null ? request.casingConvention() : project.getCasingConvention();

        if(request.prefix() != null){
            validatePrefixAgainstConvention(convention, request.prefix());
            project.setPrefix(request.prefix());
        }

        project.setCasingConvention(convention);
    }

    @Transactional
    public void deleteProject(String projectKey){
        projectsRepository.deleteByOrganisationIdAndKey(currentUser.getOrganisationId(), projectKey);
    }

    private void validatePrefixAgainstConvention(CasingConvention convention, String prefix){
        if(!convention.matches(prefix)){
            throw new InvalidPrefixException(
                    String.format("Use %s: %s", convention, convention.description())
            );
        }
    }

    public UUID findIdByKeyAndOrg(String projectKey, UUID orgId){
        return projectsRepository.findIdByKeyAndOrgId(projectKey, orgId)
                .orElseThrow(ProjectNotFoundException::new);
    }

    private Pageable sanitize(Pageable pageable) {
        if(pageable.getSort().isUnsorted()){
            return pageable;
        }

        List<String> allowed = List.of("createdAt", "name");
        List<Sort.Order> safeOrders = pageable.getSort()
                .stream()
                .filter(order -> allowed.contains(order.getProperty()))
                .toList();

        if(safeOrders.isEmpty()){
            throw new SortNotPossibleException();
        }

        Sort safeSort = Sort.by(safeOrders);

        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), safeSort);
    }
}
