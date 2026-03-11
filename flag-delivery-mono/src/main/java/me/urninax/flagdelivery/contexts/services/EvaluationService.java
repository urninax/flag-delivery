package me.urninax.flagdelivery.contexts.services;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import me.urninax.flagdelivery.contexts.events.ContextSyncEvent;
import me.urninax.flagdelivery.contexts.models.ContextInstance;
import me.urninax.flagdelivery.contexts.ui.requests.EvaluationContextRequest;
import me.urninax.flagdelivery.projectsenvs.models.environment.Environment;
import me.urninax.flagdelivery.projectsenvs.repositories.environment.EnvironmentsRepository;
import me.urninax.flagdelivery.projectsenvs.utils.exceptions.environment.EnvironmentNotFoundException;
import me.urninax.flagdelivery.shared.security.CurrentUser;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EvaluationService{
    private final ContextInstanceService contextInstanceService;
    private final CurrentUser currentUser;
    private final EnvironmentsRepository environmentsRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public JsonNode evaluate(String projectKey, String environmentKey, EvaluationContextRequest request){
        UUID orgId = currentUser.getOrganisationId();
        Environment environment = environmentsRepository.findEnvironment(orgId, projectKey, environmentKey)
                .orElseThrow(EnvironmentNotFoundException::new);

        List<ContextInstance> contextInstances = contextInstanceService.handleEvaluationContext(request);

        JsonNode evaluationResult = null; // instance evaluation using evaluation engine

        ContextSyncEvent event = new ContextSyncEvent(environment, contextInstances);
        applicationEventPublisher.publishEvent(event);

        return evaluationResult;
    }
}
