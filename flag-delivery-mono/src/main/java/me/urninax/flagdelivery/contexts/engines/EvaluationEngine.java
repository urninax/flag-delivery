package me.urninax.flagdelivery.contexts.engines;

import lombok.RequiredArgsConstructor;
import me.urninax.flagdelivery.contexts.models.ContextInstance;
import me.urninax.flagdelivery.flags.models.ContextTarget;
import me.urninax.flagdelivery.flags.models.EnvironmentFlagConfig;
import me.urninax.flagdelivery.flags.models.FlagVariation;
import me.urninax.flagdelivery.flags.models.rule.Rule;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class EvaluationEngine{
    private final RuleEvaluationEngine ruleEvaluationEngine;

    public FlagVariation evaluate(EnvironmentFlagConfig target,
                             Map<String, EnvironmentFlagConfig> efcs,
                             List<ContextInstance> contextInstances,
                             Map<String, FlagVariation> evaluationCache){

        String flagKey = target.getFlag().getKey();

        if(evaluationCache.containsKey(flagKey)){
            FlagVariation cached = evaluationCache.get(flagKey);
            if(cached == null){
                return target.getOffVariation();
            }
            return cached;
        }

        evaluationCache.put(flagKey, null);

        FlagVariation result;

        try{
            if(!target.isOn() || !arePrerequisitesSatisfied(target, efcs, contextInstances, evaluationCache)){
                result = target.getOffVariation();
            }else{
                result = resolveVariation(target, contextInstances);
            }
        }catch(Exception e){
            evaluationCache.remove(flagKey);
            throw e;
        }

        evaluationCache.put(flagKey, result);
        return result;
    }

    private boolean arePrerequisitesSatisfied(EnvironmentFlagConfig config,
                                                  Map<String, EnvironmentFlagConfig> efcs,
                                                  List<ContextInstance> contextInstances,
                                                   Map<String, FlagVariation> evaluationCache){
        return Optional.ofNullable(config.getPrerequisites())
                .orElse(Collections.emptySet())
                .stream()
                .allMatch(prerequisite -> {
                    EnvironmentFlagConfig target = efcs.get(prerequisite.getKey());
                    if(target == null || !target.isOn()) return false;

                    FlagVariation result = evaluate(target, efcs, contextInstances, evaluationCache);
                     return result != null && result.getId().equals(prerequisite.getVariation());
                });
    }

    private FlagVariation resolveVariation(EnvironmentFlagConfig target, List<ContextInstance> contextInstances){
        Optional<FlagVariation> contextTarget = handleContextTargets(target, contextInstances);

        if(contextTarget.isPresent()){
            return contextTarget.get();
        }

        Optional<FlagVariation> ruleResult = evaluateRules(target, contextInstances);

        if(ruleResult.isPresent()){
            return ruleResult.get();
        }

        return target.getOffVariation();
    }

    private Optional<FlagVariation> handleContextTargets(EnvironmentFlagConfig config,
                                                         List<ContextInstance> contextInstances){
        Set<ContextTarget> targets = config.getContextTargets();

        if(targets == null || targets.isEmpty()) return Optional.empty();

        for(ContextTarget target : config.getContextTargets()){
            for(ContextInstance context : contextInstances){
                String contextKey = context.getBody().path("key").asText("");
                String contextKind = context.getBody().path("kind").asText("");

                if(target.getContextKind().equals(contextKind) && target.getValues().contains(contextKey)){
                    return config.getFlag().getVariations().stream()
                            .filter(v ->
                                    v.getId().equals(target.getVariation()))
                            .findFirst();
                }
            }
        }

        return Optional.empty();
    }

    private Optional<FlagVariation> evaluateRules(EnvironmentFlagConfig config,
                                                  List<ContextInstance> contextInstances){
        return Optional.ofNullable(config.getRules())
                .orElse(Collections.emptySet())
                .stream()
                .sorted(Comparator.comparing(Rule::getPriority))
                .map(rule -> ruleEvaluationEngine.evaluate(rule, contextInstances))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }
}
