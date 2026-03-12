package me.urninax.flagdelivery.flags.services.patch;

import lombok.RequiredArgsConstructor;
import me.urninax.flagdelivery.flags.models.EnvironmentFlagConfig;
import me.urninax.flagdelivery.flags.models.FeatureFlag;
import me.urninax.flagdelivery.flags.models.FlagVariation;
import me.urninax.flagdelivery.flags.models.rule.Rule;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.RuleInstruction;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.rules.*;
import me.urninax.flagdelivery.flags.ui.requests.rule.RuleRequest;
import me.urninax.flagdelivery.flags.utils.exceptions.VariationNotFoundException;
import me.urninax.flagdelivery.flags.utils.exceptions.rule.RuleNotFoundException;
import me.urninax.flagdelivery.shared.exceptions.BadRequestException;
import me.urninax.flagdelivery.shared.utils.EntityMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RulesInstructionHandler{
    private final EntityMapper entityMapper;

    @Transactional
    public void handle(EnvironmentFlagConfig config, FeatureFlag flag, RuleInstruction ruleInstruction){
        switch(ruleInstruction){
            case AddRuleInstruction instruction -> addRule(config, flag, instruction);
            case RemoveRuleInstruction instruction -> removeRule(config, instruction);
            case ReorderRulesInstruction instruction -> reorderRules(config, instruction);
            case ReplaceRulesInstruction instruction -> replaceRules(config, flag, instruction);
            case UpdateRuleDescriptionInstruction instruction -> updateRuleDescription(config, instruction);
            case UpdateRuleVariationInstruction instruction -> updateRuleVariation(config, flag, instruction);
            default -> throw new BadRequestException("Unsupported rule instruction");
        }
    }

    private void addRule(EnvironmentFlagConfig config, FeatureFlag flag, AddRuleInstruction instruction){
        int targetPriority;

        if(instruction.getBeforeRuleId() != null){
            Rule referenceRule = config.getRules().stream()
                    .filter(r -> r.getId().equals(instruction.getBeforeRuleId()))
                    .findFirst()
                    .orElseThrow(RuleNotFoundException::new);

            targetPriority = referenceRule.getPriority();

            config.getRules().stream()
                    .filter(r -> r.getPriority() >= targetPriority)
                    .forEach(r -> r.setPriority(r.getPriority() + 1));
        }else {
            targetPriority = config.getRules().stream()
                    .mapToInt(Rule::getPriority)
                    .max()
                    .orElse(-1) + 1;
        }

        FlagVariation variation = flag.getVariations().stream()
                .filter(v -> v.getId().equals(instruction.getVariationId()))
                .findFirst()
                .orElseThrow(VariationNotFoundException::new);

        Rule newRule = Rule.builder()
                .variation(variation)
                .priority(targetPriority)
                .environmentFlagConfig(config)
                .build();

        instruction.getClauses().stream()
                    .map(entityMapper::toEntity)
                    .forEach(newRule::addClause);

        config.addRule(newRule);
    }

    private void removeRule(EnvironmentFlagConfig config, RemoveRuleInstruction instruction){
        Rule targetRule = config.getRules().stream()
                .filter(r -> r.getId().equals(instruction.getRuleId()))
                .findFirst()
                .orElseThrow(RuleNotFoundException::new);

        int removedPriority = targetRule.getPriority();

        config.getRules().remove(targetRule);

        config.getRules().stream()
                .filter(r -> r.getPriority() > removedPriority)
                .forEach(r -> r.setPriority(r.getPriority() - 1));
    }

    private void reorderRules(EnvironmentFlagConfig config, ReorderRulesInstruction instruction){
        List<UUID> newOrder = instruction.getRuleIds();

        Set<UUID> existingIds = config.getRules()
                .stream()
                .map(Rule::getId)
                .collect(Collectors.toSet());

        if(existingIds.size() != newOrder.size() && !existingIds.containsAll(newOrder)){
            throw new BadRequestException("The provided rules do not match the existing rules for this config.");
        }

        Map<UUID, Rule> ruleMap = config.getRules().stream()
                .collect(Collectors.toMap(Rule::getId, Function.identity()));

        for (int i = 0; i < newOrder.size(); i++) {
            UUID ruleId = newOrder.get(i);
            ruleMap.get(ruleId).setPriority(i);
        }
    }

    private void replaceRules(EnvironmentFlagConfig config, FeatureFlag flag, ReplaceRulesInstruction instruction){
        List<RuleRequest> requests = instruction.getRules();

        List<Rule> finalRules = new ArrayList<>();

        for(int i = 0; i < requests.size(); i++){
            RuleRequest ruleRequest = requests.get(i);

            FlagVariation variation = flag.getVariations().stream()
                    .filter(v -> v.getId().equals(ruleRequest.getVariationId()))
                    .findFirst()
                    .orElseThrow(VariationNotFoundException::new);

            Rule rule = Rule.builder()
                    .description(ruleRequest.getDescription())
                    .environmentFlagConfig(config)
                    .variation(variation)
                    .priority(i)
                    .build();

            ruleRequest.getClauses().stream()
                    .map(entityMapper::toEntity)
                    .forEach(rule::addClause);

            finalRules.add(rule);
        }

        config.getRules().clear();
        config.getRules().addAll(finalRules);
    }

    private void updateRuleDescription(EnvironmentFlagConfig config, UpdateRuleDescriptionInstruction instruction){
        Rule rule = config.getRules().stream()
                .filter(r -> r.getId().equals(instruction.getRuleId()))
                .findFirst()
                .orElseThrow(RuleNotFoundException::new);

        rule.setDescription(instruction.getDescription());
    }

    private void updateRuleVariation(EnvironmentFlagConfig config, FeatureFlag flag, UpdateRuleVariationInstruction instruction){
        Rule rule = config.getRules().stream()
                .filter(r -> r.getId().equals(instruction.getRuleId()))
                .findFirst()
                .orElseThrow(RuleNotFoundException::new);

        FlagVariation variation = flag.getVariations().stream()
                .filter(v -> v.getId().equals(instruction.getVariationId()))
                .findFirst().orElseThrow(VariationNotFoundException::new);

        rule.setVariation(variation);
    }
}
