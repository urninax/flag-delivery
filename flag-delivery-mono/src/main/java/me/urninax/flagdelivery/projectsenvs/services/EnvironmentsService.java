package me.urninax.flagdelivery.projectsenvs.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import me.urninax.flagdelivery.projectsenvs.models.environment.Environment;
import me.urninax.flagdelivery.projectsenvs.models.environment.EnvironmentTag;
import me.urninax.flagdelivery.projectsenvs.models.environment.EnvironmentTagId;
import me.urninax.flagdelivery.projectsenvs.repositories.EnvironmentsRepository;
import me.urninax.flagdelivery.projectsenvs.repositories.ProjectsRepository;
import me.urninax.flagdelivery.projectsenvs.shared.CommonSpecifications;
import me.urninax.flagdelivery.projectsenvs.shared.environment.EnvironmentDTO;
import me.urninax.flagdelivery.projectsenvs.shared.environment.EnvironmentSpecifications;
import me.urninax.flagdelivery.projectsenvs.ui.models.requests.environment.CreateEnvironmentRequest;
import me.urninax.flagdelivery.projectsenvs.ui.models.requests.environment.ListAllEnvironmentsRequest;
import me.urninax.flagdelivery.projectsenvs.ui.models.requests.environment.PatchEnvironmentRequest;
import me.urninax.flagdelivery.shared.exceptions.ConflictException;
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

    @Transactional
    public EnvironmentDTO createEnvironment(String projectKey, CreateEnvironmentRequest request){
        UUID orgId = currentUser.getOrganisationId();
        UUID projectId = projectsRepository.findIdByKeyAndOrgId(projectKey, orgId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project was not found"));

        Environment environment = Environment.builder()
                .name(request.name().trim().replaceAll("\\s+", " "))
                .key(request.key())
                .project(projectsRepository.getReferenceById(projectId))
                .confirmChanges(request.confirmChanges())
                .requireComments(request.requireComments())
                .critical(request.critical())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        Set<EnvironmentTag> tags = request.tags()
                .stream()
                .map(tag -> new EnvironmentTag(
                        new EnvironmentTagId(null, tag), environment))
                .collect(Collectors.toSet());

        environment.setTags(tags);

        try{
            Environment createdEnv = environmentsRepository.saveAndFlush(environment);
            return entityMapper.toDTO(createdEnv);
        }catch(DataIntegrityViolationException exc){
            if(PersistenceExceptionUtils.isUniqueException(exc)){
                throw new ConflictException("Environment key already in use");
            }
            throw exc;
        }
    }

    public EnvironmentDTO getEnvironment(String projectKey, String environmentKey){
        UUID orgId = currentUser.getOrganisationId();
        Environment environment = environmentsRepository.findEnvironment(orgId, projectKey, environmentKey)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Environment was not found"));

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

        List<Environment> environments = environmentsRepository.findAll(envSpec, sanitize(sort));

        return environments.stream().map(entityMapper::toDTO).toList();
    }

    @Transactional
    public EnvironmentDTO patchEnvironment(String projectKey, String environmentKey, PatchEnvironmentRequest request){
        UUID orgId = currentUser.getOrganisationId();
        Environment environment = environmentsRepository.findEnvironment(orgId, projectKey, environmentKey)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Environment was not found"));

        if(request.name() != null && !request.name().isBlank()){
            environment.setName(request.name().trim().replaceAll("\\s+", " "));
        }

        if(request.confirmChanges() != null){
            environment.setConfirmChanges(request.confirmChanges());
        }

        if(request.requireComments() != null){
            environment.setRequireComments(request.requireComments());
        }

        if(request.critical() != null){
            environment.setCritical(request.critical());
        }

        if(request.tags() != null){
            Set<EnvironmentTag> envTags = request.tags().stream()
                    .map(et -> new EnvironmentTag(new EnvironmentTagId(null, et), environment))
                    .collect(Collectors.toSet());
            environment.getTags().clear();
            environment.getTags().addAll(envTags);
        }

        Environment savedEnv = environmentsRepository.saveAndFlush(environment);

        return entityMapper.toDTO(savedEnv);
    }

    @Transactional
    public void deleteEnvironment(String projectKey, String environmentKey){
        UUID orgId = currentUser.getOrganisationId();
        int envCount = environmentsRepository.countEnvironmentByOrgIdAndProjectKey(orgId, projectKey);

        if(envCount == 1){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Project must have at least one environment");
        }

        Environment environment = environmentsRepository.findEnvironment(orgId, projectKey, environmentKey)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Environment was not found"));

        environmentsRepository.delete(environment);
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
}
