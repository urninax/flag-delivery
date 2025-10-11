package me.urninax.flagdelivery.projectsenvs.services;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import me.urninax.flagdelivery.projectsenvs.models.environment.Environment;
import me.urninax.flagdelivery.projectsenvs.models.environment.EnvironmentTag;
import me.urninax.flagdelivery.projectsenvs.models.environment.EnvironmentTagId;
import me.urninax.flagdelivery.projectsenvs.models.project.Project;
import me.urninax.flagdelivery.projectsenvs.repositories.EnvironmentsRepository;
import me.urninax.flagdelivery.projectsenvs.repositories.ProjectsRepository;
import me.urninax.flagdelivery.projectsenvs.shared.CommonSpecifications;
import me.urninax.flagdelivery.projectsenvs.shared.environment.EnvironmentDTO;
import me.urninax.flagdelivery.projectsenvs.shared.environment.EnvironmentSpecifications;
import me.urninax.flagdelivery.projectsenvs.ui.models.requests.environment.CreateEnvironmentRequest;
import me.urninax.flagdelivery.projectsenvs.ui.models.requests.environment.ListAllEnvironmentsRequest;
import me.urninax.flagdelivery.projectsenvs.ui.models.requests.environment.PatchEnvironmentRequest;
import me.urninax.flagdelivery.projectsenvs.utils.exceptions.environment.EnvironmentAlreadyExistsException;
import me.urninax.flagdelivery.projectsenvs.utils.exceptions.project.ProjectNotFoundException;
import me.urninax.flagdelivery.projectsenvs.utils.exceptions.environment.EnvironmentNotFoundException;
import me.urninax.flagdelivery.projectsenvs.utils.exceptions.environment.MissingEnvironmentException;
import me.urninax.flagdelivery.shared.security.CurrentUser;
import me.urninax.flagdelivery.shared.utils.EntityMapper;
import me.urninax.flagdelivery.shared.utils.PersistenceExceptionUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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

    public List<EnvironmentDTO> listEnvironments(String projectKey, ListAllEnvironmentsRequest request, Sort sort){
        UUID orgId = currentUser.getOrganisationId();
        Specification<Environment> envSpec = EnvironmentSpecifications.byOrgAndProjectKey(orgId, projectKey);

        if(request != null){
            if(!request.query().isBlank()){
                envSpec = envSpec.and(CommonSpecifications.hasQuery(request.query()));
            }

            if(!request.tags().isEmpty()){
                envSpec = envSpec.and(EnvironmentSpecifications.hasAllTags(request.tags()));
            }
        }

        return environmentsRepository.findAll(envSpec, sanitize(sort))
                .stream()
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
            environment.setTags(toTags(request.tags(), environment));
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
            env.getTags().clear();
            env.getTags().addAll(toTags(request.tags(), env));
        }
        env.setUpdatedAt(Instant.now());
    }

    private Set<EnvironmentTag> toTags(List<String> tags, Environment env) {
        return tags.stream()
                .map(tag -> new EnvironmentTag(new EnvironmentTagId(null, tag), env))
                .collect(Collectors.toSet());
    }

    private Sort sanitize(Sort sort) {
        if(sort.isUnsorted()){
            return sort;
        }

        List<String> allowed = List.of("createdAt", "critical", "name");
        List<Sort.Order> safeOrders = sort
                .stream()
                .filter(order -> allowed.contains(order.getProperty()))
                .toList();

        if(safeOrders.isEmpty()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot be sorted");
        }

        return Sort.by(safeOrders);
    }

    private boolean defaultIfNull(Boolean value, boolean defaultVal){
        return value != null ? value : defaultVal;
    }
}
