package me.urninax.flagdelivery.contexts.events;

import me.urninax.flagdelivery.contexts.models.ContextInstance;
import me.urninax.flagdelivery.projectsenvs.models.environment.Environment;

import java.util.List;

public record ContextSyncEvent(Environment environment, List<ContextInstance> contextInstances){ }