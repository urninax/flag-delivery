package me.urninax.flagdelivery.contexts.services;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import me.urninax.flagdelivery.contexts.engines.EvaluationEngine;
import me.urninax.flagdelivery.contexts.events.ContextSyncEvent;
import me.urninax.flagdelivery.contexts.models.ContextInstance;
import me.urninax.flagdelivery.contexts.ui.requests.EvaluationContextRequest;
import me.urninax.flagdelivery.flags.models.EnvironmentFlagConfig;
import me.urninax.flagdelivery.flags.models.FlagVariation;
import me.urninax.flagdelivery.flags.repositories.FlagConfigsRepository;
import me.urninax.flagdelivery.projectsenvs.models.environment.Environment;
import me.urninax.flagdelivery.projectsenvs.repositories.environment.EnvironmentsRepository;
import me.urninax.flagdelivery.projectsenvs.utils.exceptions.environment.EnvironmentNotFoundException;
import me.urninax.flagdelivery.shared.security.CurrentUser;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EvaluationService{
    private final ContextInstanceService contextInstanceService;
    private final CurrentUser currentUser;
    private final EnvironmentsRepository environmentsRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final FlagConfigsRepository flagConfigsRepository;

    private final EvaluationEngine evaluationEngine;

    public Map<String, JsonNode> evaluateFlags(String projectKey, String environmentKey, EvaluationContextRequest request){
        UUID orgId = currentUser.getOrganisationId();
        Environment environment = environmentsRepository.findEnvironment(orgId, projectKey, environmentKey)
                .orElseThrow(EnvironmentNotFoundException::new);

        List<ContextInstance> contextInstances = contextInstanceService.handleEvaluationContext(request);

        List<EnvironmentFlagConfig> allConfigs = flagConfigsRepository.findAllByEnvironmentIdDeep(environment.getId());

        Map<String, EnvironmentFlagConfig> configMap = allConfigs.stream()
                .collect(Collectors.toMap(c -> c.getFlag().getKey(), Function.identity()));

        Map<String, FlagVariation> evaluationCache = new HashMap<>();

        Map<String, JsonNode> evaluationResults = new HashMap<>();

        for(EnvironmentFlagConfig efc : allConfigs){
            FlagVariation evaluationResult = evaluationEngine.evaluate(efc, configMap, contextInstances, evaluationCache);

            evaluationResults.put(efc.getFlag().getKey(), evaluationResult.getValue());
        }

        ContextSyncEvent event = new ContextSyncEvent(environment, contextInstances);
        applicationEventPublisher.publishEvent(event);

        return evaluationResults;
    }
}
