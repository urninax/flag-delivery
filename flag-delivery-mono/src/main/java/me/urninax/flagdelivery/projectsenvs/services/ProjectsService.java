package me.urninax.flagdelivery.projectsenvs.services;

import jakarta.transaction.Transactional;
import me.urninax.flagdelivery.projectsenvs.models.project.CasingConvention;
import me.urninax.flagdelivery.projectsenvs.models.project.Project;
import me.urninax.flagdelivery.projectsenvs.models.project.ProjectTag;
import me.urninax.flagdelivery.projectsenvs.models.project.ProjectTagId;
import me.urninax.flagdelivery.projectsenvs.repositories.ProjectTagsRepository;
import me.urninax.flagdelivery.projectsenvs.repositories.ProjectsRepository;
import me.urninax.flagdelivery.projectsenvs.shared.project.ProjectDTO;
import me.urninax.flagdelivery.projectsenvs.shared.project.ProjectSpecifications;
import me.urninax.flagdelivery.projectsenvs.ui.models.requests.CreateProjectRequest;
import me.urninax.flagdelivery.projectsenvs.ui.models.requests.ListAllProjectsRequest;
import me.urninax.flagdelivery.projectsenvs.ui.models.requests.PatchProjectRequest;
import me.urninax.flagdelivery.shared.exceptions.ConflictException;
import me.urninax.flagdelivery.shared.security.CurrentUser;
import me.urninax.flagdelivery.shared.utils.EntityMapper;
import me.urninax.flagdelivery.shared.utils.PersistenceExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProjectsService{
    private final ProjectsRepository projectsRepository;
    private final EntityMapper entityMapper;
    private final CurrentUser currentUser;
    private final Clock clock;
    private final ProjectTagsRepository projectTagsRepository;

    @Autowired
    public ProjectsService(ProjectsRepository projectsRepository, EntityMapper entityMapper, CurrentUser currentUser, Clock clock, ProjectTagsRepository projectTagsRepository){
        this.projectsRepository = projectsRepository;
        this.entityMapper = entityMapper;
        this.currentUser = currentUser;
        this.clock = clock;
        this.projectTagsRepository = projectTagsRepository;
    }

    @Transactional
    public ProjectDTO createProject(CreateProjectRequest request){
        UUID userId = currentUser.getUserId();
        UUID organisationId = currentUser.getOrganisationId();

        Project project = Project.builder()
                .name(request.name().trim().replaceAll("\\s+", " "))
                .key(request.key())
                .organisationId(organisationId)
                .createdBy(userId)
                .createdAt(Instant.now(clock))
                .updatedAt(Instant.now(clock))
                .build();

        if(request.namingConvention() != null){
            CasingConvention convention = request.namingConvention().casingConvention();
            String prefix = request.namingConvention().prefix();

            if(convention != null){
                if(prefix != null && !convention.matches(prefix)){
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            String.format("Use %s: %s", convention, convention.description())
                    );
                }
            }

            project.setCasingConvention(request.namingConvention().casingConvention());
            project.setPrefix(request.namingConvention().prefix());
        }
        Set<ProjectTag> tags = request.tags()
                .stream()
                .map(tag -> new ProjectTag(
                        new ProjectTagId(null, tag), project))
                .collect(Collectors.toSet());

        project.setTags(tags);

        //todo: create default environments if not specified (publish event maybe)
        try{
            Project createdProject = projectsRepository.saveAndFlush(project);
            return entityMapper.toDTO(createdProject);
        }catch(DataIntegrityViolationException exc){
            if(PersistenceExceptionUtils.isUniqueException(exc)){
                throw new ConflictException("Project key already in use");
            }
            throw exc;
        }
    }

    public ProjectDTO getProject(String projectKey){
        Project project = projectsRepository.findByOrganisationIdAndKey(currentUser.getOrganisationId(), projectKey)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project was not found"));

        return entityMapper.toDTO(project);
    }

    public Page<ProjectDTO> getPaginatedProjects(ListAllProjectsRequest request, Pageable pageable){
        Specification<Project> projectSpec = ProjectSpecifications.byOrganisation(currentUser.getOrganisationId());

        if(request != null){
            if(!request.query().isBlank()){
                projectSpec = projectSpec.and(ProjectSpecifications.hasQuery(request.query()));
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

        List<UUID> ids = projectPage.getContent().stream().map(Project::getId).toList();
        if (!ids.isEmpty()) {
            List<ProjectTag> allTags = projectTagsRepository.findAllByProjectIdIn(ids);
            Map<UUID, Set<ProjectTag>> byProject = allTags.stream()
                    .collect(Collectors.groupingBy(pt -> pt.getProject().getId(),
                            Collectors.toCollection(LinkedHashSet::new)));
            projectPage.getContent().forEach(p ->
                    p.setTags(byProject.getOrDefault(p.getId(), Set.of()))
            );
        }

        return projectPage.map(entityMapper::toDTO);
    }

    @Transactional
    public void patchProject(String projectKey, PatchProjectRequest request){
        if(request.name() == null && request.tags() == null){
            return;
        }

        Project project = projectsRepository.findByOrganisationIdAndKey(currentUser.getOrganisationId(), projectKey)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project was not found"));

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
    }

    @Transactional
    public void deleteProject(String projectKey){
        projectsRepository.deleteByOrganisationIdAndKey(currentUser.getOrganisationId(), projectKey);
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot be sorted");
        }

        Sort safeSort = Sort.by(safeOrders);

        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), safeSort);
    }
}
