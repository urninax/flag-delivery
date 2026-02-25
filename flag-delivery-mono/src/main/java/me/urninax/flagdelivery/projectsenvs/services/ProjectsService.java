package me.urninax.flagdelivery.projectsenvs.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import me.urninax.flagdelivery.projectsenvs.models.environment.Environment;
import me.urninax.flagdelivery.projectsenvs.models.project.CasingConvention;
import me.urninax.flagdelivery.projectsenvs.models.project.Project;
import me.urninax.flagdelivery.projectsenvs.repositories.project.ProjectsRepository;
import me.urninax.flagdelivery.projectsenvs.shared.project.ProjectDTO;
import me.urninax.flagdelivery.projectsenvs.ui.models.requests.project.CreateProjectRequest;
import me.urninax.flagdelivery.projectsenvs.ui.models.requests.project.ListAllProjectsRequest;
import me.urninax.flagdelivery.projectsenvs.ui.models.requests.project.NamingConventionRequest;
import me.urninax.flagdelivery.projectsenvs.ui.models.requests.project.PatchProjectRequest;
import me.urninax.flagdelivery.projectsenvs.utils.exceptions.project.InvalidPrefixException;
import me.urninax.flagdelivery.projectsenvs.utils.exceptions.project.ProjectAlreadyExistsException;
import me.urninax.flagdelivery.projectsenvs.utils.exceptions.project.ProjectNotFoundException;
import me.urninax.flagdelivery.projectsenvs.utils.exceptions.environment.EnvironmentAlreadyExistsException;
import me.urninax.flagdelivery.shared.security.CurrentUser;
import me.urninax.flagdelivery.shared.utils.EntityMapper;
import me.urninax.flagdelivery.shared.utils.PersistenceExceptionUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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

        project.addTags(new HashSet<>(request.tags()));

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

    public List<ProjectDTO> getProjects(ListAllProjectsRequest request, Sort sort){
        UUID orgId = currentUser.getOrganisationId();

        List<Project> projects = projectsRepository.findAllWithFilter(orgId, request, sort);

        return projects.stream()
                .map(entityMapper::toDTO)
                .toList();
    }

    public Page<ProjectDTO> getPaginatedProjects(ListAllProjectsRequest request, Pageable pageable){
        UUID orgId = currentUser.getOrganisationId();

        Page<Project> projectPage = projectsRepository.findPageWithFilter(orgId, request, pageable);

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
            project.addTags(new HashSet<>(request.tags()));
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
}
