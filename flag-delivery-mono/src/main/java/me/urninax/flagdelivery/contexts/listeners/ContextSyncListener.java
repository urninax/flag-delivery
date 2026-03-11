package me.urninax.flagdelivery.contexts.listeners;

import lombok.RequiredArgsConstructor;
import me.urninax.flagdelivery.contexts.events.ContextSyncEvent;
import me.urninax.flagdelivery.contexts.services.ContextService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ContextSyncListener{
    private final ContextService contextService;

    @EventListener
    public void onEvaluation(ContextSyncEvent event){
        contextService.syncContexts(event.environment(), event.contextInstances());
    }
}
