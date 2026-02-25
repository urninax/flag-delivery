package me.urninax.flagdelivery.projectsenvs.services;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import me.urninax.flagdelivery.projectsenvs.models.environment.Environment;
import me.urninax.flagdelivery.projectsenvs.models.project.Project;
import me.urninax.flagdelivery.projectsenvs.repositories.environment.EnvironmentsRepository;
import me.urninax.flagdelivery.projectsenvs.repositories.project.ProjectsRepository;
import me.urninax.flagdelivery.projectsenvs.shared.environment.EnvironmentDTO;
import me.urninax.flagdelivery.projectsenvs.ui.models.requests.environment.CreateEnvironmentRequest;
import me.urninax.flagdelivery.projectsenvs.ui.models.requests.environment.ListAllEnvironmentsRequest;
import me.urninax.flagdelivery.projectsenvs.ui.models.requests.environment.PatchEnvironmentRequest;
import me.urninax.flagdelivery.projectsenvs.utils.exceptions.environment.EnvironmentAlreadyExistsException;
import me.urninax.flagdelivery.projectsenvs.utils.exceptions.environment.EnvironmentNotFoundException;
import me.urninax.flagdelivery.projectsenvs.utils.exceptions.environment.MissingEnvironmentException;
import me.urninax.flagdelivery.projectsenvs.utils.exceptions.project.ProjectNotFoundException;
import me.urninax.flagdelivery.shared.security.CurrentUser;
import me.urninax.flagdelivery.shared.utils.EntityMapper;
import me.urninax.flagdelivery.shared.utils.PersistenceExceptionUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EnvironmentsService{
    private final EnvironmentsRepository environmentsRepository;
    private final ProjectsRepository projectsRepository;
    private final EntityMapper entityMapper;
    private final CurrentUser currentUser;
    private final EntityManager em;

    // ----------------
    // CRUD
    // ----------------

    @Transactional
    public EnvironmentDTO createEnvironment(String projectKey, CreateEnvironmentRequest request){
        UUID orgId = currentUser.getOrganisationId();
        UUID projectId = projectsRepository.findIdByKeyAndOrgId(projectKey, orgId)
                .orElseThrow(ProjectNotFoundException::new);

        Project project = em.getReference(Project.class, projectId);

        Environment environment = buildEnvironment(request, project);
        environment.setProject(projectsRepository.getReferenceById(projectId));

        try{
            Environment createdEnv = environmentsRepository.saveAndFlush(environment);
            return entityMapper.toDTO(createdEnv);
        }catch(DataIntegrityViolationException exc){
            if(PersistenceExceptionUtils.isUniqueException(exc)){
                throw new EnvironmentAlreadyExistsException();
            }
            throw exc;
        }

        //todo: create flag configs for each feature flag
    }

    public EnvironmentDTO getEnvironment(String projectKey, String environmentKey){
        UUID orgId = currentUser.getOrganisationId();
        Environment environment = environmentsRepository.findEnvironment(orgId, projectKey, environmentKey)
                .orElseThrow(EnvironmentNotFoundException::new);

        return entityMapper.toDTO(environment);
    }

    public Page<EnvironmentDTO> getPaginatedEnvironments(String projectKey, Pageable pageable, ListAllEnvironmentsRequest request){
        UUID orgId = currentUser.getOrganisationId();
        Page<Environment> environmentPage = environmentsRepository.findPageWithFilter(orgId, projectKey, request, pageable);

        return environmentPage.map(entityMapper::toDTO);
    }

    public List<EnvironmentDTO> listEnvironments(String projectKey, ListAllEnvironmentsRequest request, Sort sort){
        UUID orgId = currentUser.getOrganisationId();

        List<Environment> environments = environmentsRepository.findAllWithFilter(orgId, projectKey, request, sort);

        return environments.stream()
                .map(entityMapper::toDTO)
                .toList();
    }

    @Transactional
    public EnvironmentDTO patchEnvironment(String projectKey, String environmentKey, PatchEnvironmentRequest request){
        UUID orgId = currentUser.getOrganisationId();
        Environment environment = environmentsRepository.findEnvironment(orgId, projectKey, environmentKey)
                .orElseThrow(EnvironmentNotFoundException::new);

        applyPatch(environment, request);

        Environment savedEnv = environmentsRepository.saveAndFlush(environment);

        return entityMapper.toDTO(savedEnv);
    }

    @Transactional
    public void deleteEnvironment(String projectKey, String environmentKey){
        UUID orgId = currentUser.getOrganisationId();
        int envCount = environmentsRepository.countEnvironmentByOrgIdAndProjectKey(orgId, projectKey);

        if(envCount == 1){
            throw new MissingEnvironmentException();
        }

        Environment environment = environmentsRepository.findEnvironment(orgId, projectKey, environmentKey)
                .orElseThrow(EnvironmentNotFoundException::new);

        environmentsRepository.delete(environment);
    }

    // -----------------
    // Helpers
    // -----------------

    public List<Environment> generateDefaultEnvironments(Project project){
        CreateEnvironmentRequest request = CreateEnvironmentRequest.builder()
                .name("Production")
                .key("default")
                .confirmChanges(true)
                .requireComments(true)
                .critical(true)
                .build();

        return List.of(buildEnvironment(request, project));
    }

    public Environment buildEnvironment(CreateEnvironmentRequest request, Project project){
        Environment environment = Environment.builder()
                .name(request.name().trim().replaceAll("\\s+", " "))
                .key(request.key())
                .confirmChanges(defaultIfNull(request.confirmChanges(), false))
                .requireComments(defaultIfNull(request.requireComments(), false))
                .critical(defaultIfNull(request.critical(), false))
                .project(project)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        if(request.tags() != null){
            environment.addTags(new HashSet<>(request.tags()));
        }

        return environment;
    }

    private void applyPatch(Environment env, PatchEnvironmentRequest request) {
        if (request.name() != null && !request.name().isBlank()) {
            env.setName(request.name().trim().replaceAll("\\s+", " "));
        }
        if (request.confirmChanges() != null) {
            env.setConfirmChanges(request.confirmChanges());
        }
        if (request.requireComments() != null) {
            env.setRequireComments(request.requireComments());
        }
        if (request.critical() != null) {
            env.setCritical(request.critical());
        }
        if (request.tags() != null) {
            env.addTags(new HashSet<>(request.tags()));
        }
        env.setUpdatedAt(Instant.now());
    }

    private boolean defaultIfNull(Boolean value, boolean defaultVal){
        return value != null ? value : defaultVal;
    }
}
