package me.urninax.flagdelivery.contexts.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import me.urninax.flagdelivery.contexts.models.ContextKind;
import me.urninax.flagdelivery.contexts.repositories.ContextKindRepository;
import me.urninax.flagdelivery.contexts.shared.ContextKindDTO;
import me.urninax.flagdelivery.contexts.ui.requests.CreateContextKindRequest;
import me.urninax.flagdelivery.projectsenvs.models.project.Project;
import me.urninax.flagdelivery.projectsenvs.repositories.project.ProjectsRepository;
import me.urninax.flagdelivery.projectsenvs.services.validation.KeyType;
import me.urninax.flagdelivery.projectsenvs.services.validation.ValidKey;
import me.urninax.flagdelivery.projectsenvs.utils.exceptions.project.ProjectNotFoundException;
import me.urninax.flagdelivery.shared.security.CurrentUser;
import me.urninax.flagdelivery.shared.utils.EntityMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.Clock;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Validated
public class ContextKindService{
    private final ContextKindRepository contextKindRepository;
    private final ProjectsRepository projectsRepository;
    private final CurrentUser currentUser;
    private final Clock clock;
    private final EntityMapper entityMapper;
    private final ObjectMapper objectMapper;

    @Transactional
    public ContextKindDTO createOrUpdateContextKind(String projectKey,
                                                    @Size(min = 2, max = 64, message = "Context kind key should be 2-64 characters.")
                                                 @NotEmpty(message = "Context kind key cannot be empty")
                                                 @ValidKey(type = KeyType.CONTEXTKIND) String contextKindKey,
                                                    CreateContextKindRequest request){
        UUID orgId = currentUser.getOrganisationId();
        Project project = projectsRepository.findByOrganisationIdAndKey(orgId, projectKey).orElseThrow(ProjectNotFoundException::new);


        ContextKind contextKind = contextKindRepository.findByProjectIdAndKey(project.getId(), contextKindKey)
                .map(existing -> {
                    existing.setName(request.name());

                    if(request.description().isPresent()){
                        existing.setDescription(request.description().get());
                    }

                    existing.setUpdatedAt(clock.instant());
                    return existing;
                })
                .orElseGet(() -> ContextKind.builder()
                        .projectId(project.getId())
                        .name(request.name())
                        .key(contextKindKey)
                        .description(request.description().orElse(null))
                        .createdAt(clock.instant())
                        .updatedAt(clock.instant())
                        .build()
                );

        return entityMapper.toDTO(contextKindRepository.saveAndFlush(contextKind));
    }

    public ContextKind createOrGetContextKind(String contextKindKey, UUID projectId){
        return contextKindRepository.findByProjectIdAndKey(projectId, contextKindKey)
                .orElseGet(() -> ContextKind.builder()
                        .projectId(projectId)
                        .name(contextKindKey)
                        .key(contextKindKey)
                        .createdAt(clock.instant())
                        .updatedAt(clock.instant())
                        .build()
                );
    }

    // helper methods

    public void updateContextKindAttributes(ContextKind contextKind, JsonNode evaluationBody){
        ObjectNode existingPaths = (ObjectNode) contextKind.getAttributes();
        if(existingPaths == null){
            existingPaths = objectMapper.createObjectNode();
        }

        extractPaths("", evaluationBody, existingPaths);

        contextKind.setAttributes(existingPaths);
    }

    private void extractPaths(String currentPath, JsonNode evaluationBody, ObjectNode collector){
        if(evaluationBody.isObject()){
            evaluationBody.forEachEntry((attribute, value) -> {
                String nextPath = currentPath + "/" + attribute;
                extractPaths(nextPath, value, collector);
            });
        }else{
            collector.put(currentPath, evaluationBody.asText());
        }
    }

}
