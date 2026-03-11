package me.urninax.flagdelivery.contexts.services;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import me.urninax.flagdelivery.contexts.models.Context;
import me.urninax.flagdelivery.contexts.models.ContextInstance;
import me.urninax.flagdelivery.contexts.models.ContextKind;
import me.urninax.flagdelivery.contexts.repositories.ContextInstanceRepository;
import me.urninax.flagdelivery.contexts.repositories.ContextKindRepository;
import me.urninax.flagdelivery.contexts.repositories.ContextRepository;
import me.urninax.flagdelivery.projectsenvs.models.environment.Environment;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ContextService{
    private final Clock clock;
    private final ContextRepository contextRepository;
    private final ContextKindService contextKindService;
    private final ContextKindRepository contextKindRepository;
    private final ContextInstanceRepository contextInstanceRepository;

    public void syncContexts(Environment environment, List<ContextInstance> contextInstances){
        List<ContextInstance> persistentInstances = contextInstances.stream()
                .map(ti -> contextInstanceRepository.findByHash(ti.getHash())
                        .orElseGet(() -> contextInstanceRepository.save(ti)))
                .toList();

        Map<String, ContextKind> kindMap = new HashMap<>();
        for(ContextInstance instance : contextInstances){
            String kindKey = instance.getBody().get("kind").asText();
            JsonNode attributes = instance.getBody().get("attributes");

            ContextKind kind = kindMap.computeIfAbsent(kindKey, k ->
                    contextKindService.createOrGetContextKind(k, environment.getProject().getId()));

            contextKindService.updateContextKindAttributes(kind, attributes);
        }

        contextKindRepository.saveAllAndFlush(kindMap.values());

        List<Context> contextsToSave = persistentInstances.stream().map(instance -> {
            String kindKey = instance.getBody().get("kind").asText();
            String contextKey = instance.getBody().get("key").asText();

            ContextKind kind = kindMap.get(kindKey);

            Context context = contextRepository.findByEnvironmentIdAndKey(environment.getId(), contextKey)
                    .orElseGet(() -> createContext(kind, environment, contextKey));

            context.addInstance(instance);
            context.setLastSeen(clock.instant());
            return context;
        }).toList();

        contextRepository.saveAll(contextsToSave);
    }

    private Context createContext(ContextKind contextKind, Environment environment, String contextKey){
        return Context.builder()
                .contextKind(contextKind)
                .environment(environment)
                .key(contextKey)
                .createdAt(clock.instant())
                .build();
    }
}
