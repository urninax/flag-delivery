package me.urninax.flagdelivery.contexts.services;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import me.urninax.flagdelivery.contexts.models.Context;
import me.urninax.flagdelivery.contexts.models.ContextInstance;
import me.urninax.flagdelivery.contexts.models.ContextKind;
import me.urninax.flagdelivery.contexts.repositories.ContextRepository;
import me.urninax.flagdelivery.projectsenvs.models.environment.Environment;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContextService{
    private final Clock clock;
    private final ContextRepository contextRepository;
    private final ContextKindService contextKindService;
    private final ContextService contextService;

    public Context createContext(ContextKind contextKind, Environment environment, String contextKey){
        return Context.builder()
                .contextKind(contextKind)
                .environment(environment)
                .key(contextKey)
                .createdAt(clock.instant())
                .build();
    }

    public void syncContexts(Environment environment, List<ContextInstance> contextInstances){
        List<Context> contextsToSave = contextInstances.stream().map(instance -> {
            String kindKey = instance.getBody().get("kind").asText();
            String contextKey = instance.getBody().get("key").asText();
            JsonNode attributes = instance.getBody().get("attributes");

            ContextKind kind = contextKindService.createOrGetContextKind(kindKey, environment.getProject().getId());
            contextKindService.updateContextKindAttributes(kind, attributes);

            Context context = contextRepository.findByEnvironmentIdAndKey(environment.getId(), contextKey)
                    .orElseGet(() -> contextService.createContext(kind, environment, contextKey));

            context.addInstance(instance);
            context.setLastSeen(clock.instant());
            return context;
        }).toList();

        contextRepository.saveAll(contextsToSave);
    }
}
