package me.urninax.flagdelivery.projectsenvs.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import me.urninax.flagdelivery.projectsenvs.models.environment.Environment;
import me.urninax.flagdelivery.projectsenvs.models.environment.EnvironmentTag;
import me.urninax.flagdelivery.projectsenvs.models.environment.EnvironmentTagId;
import me.urninax.flagdelivery.projectsenvs.repositories.EnvironmentsRepository;
import me.urninax.flagdelivery.projectsenvs.repositories.ProjectsRepository;
import me.urninax.flagdelivery.projectsenvs.shared.environment.EnvironmentDTO;
import me.urninax.flagdelivery.projectsenvs.ui.models.requests.environment.CreateEnvironmentRequest;
import me.urninax.flagdelivery.shared.exceptions.ConflictException;
import me.urninax.flagdelivery.shared.security.CurrentUser;
import me.urninax.flagdelivery.shared.utils.EntityMapper;
import me.urninax.flagdelivery.shared.utils.PersistenceExceptionUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
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
                .projectId(projectId)
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
}
