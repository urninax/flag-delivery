package me.urninax.flagdelivery.flags.services.patch;

import me.urninax.flagdelivery.flags.models.ContextTarget;
import me.urninax.flagdelivery.flags.models.EnvironmentFlagConfig;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.TargetInstruction;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.targets.AddTargetsInstruction;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.targets.ClearTargetsInstruction;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.targets.RemoveTargetsInstruction;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.targets.ReplaceTargetsInstruction;
import me.urninax.flagdelivery.shared.exceptions.BadRequestException;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class TargetsInstructionHandler{
    public void handle(EnvironmentFlagConfig config, TargetInstruction targetInstruction){
        switch(targetInstruction){
            case AddTargetsInstruction instruction -> addTargets(config, instruction);
            case ClearTargetsInstruction instruction -> clearTargets(config, instruction);
            case RemoveTargetsInstruction instruction -> removeTargets(config, instruction);
            case ReplaceTargetsInstruction instruction -> replaceTargets(config, instruction);
            default -> throw new BadRequestException("Unsupported target instruction");
        }
    }

    private void addTargets(EnvironmentFlagConfig config, AddTargetsInstruction instruction){
        UUID variationId = instruction.getVariationId();

        validateVariationExists(config, variationId);

        String contextKind = instruction.getContextKind();
        List<String> values = instruction.getValues();

        ContextTarget target = config.getContextTargets().stream()
                .filter(t -> t.getVariation().equals(variationId) && t.getContextKind().equals(contextKind))
                .findFirst()
                .orElseGet(() -> {
                    ContextTarget newTarget = ContextTarget.builder()
                            .variation(variationId)
                            .contextKind(contextKind)
                            .values(new HashSet<>())
                            .build();
                    config.getContextTargets().add(newTarget);
                    return newTarget;
                });

        target.getValues().addAll(values);
    }

    private void clearTargets(EnvironmentFlagConfig config, ClearTargetsInstruction instruction){
        UUID variationId = instruction.getVariationId();
        config.getContextTargets().removeIf(t -> t.getVariation().equals(variationId));
    }

    private void removeTargets(EnvironmentFlagConfig config, RemoveTargetsInstruction instruction){
        UUID variationId = instruction.getVariationId();
        List<String> valuesToRemove = instruction.getValues();

        config.getContextTargets().stream()
                .filter(t -> t.getVariation().equals(variationId))
                .forEach(t -> valuesToRemove.forEach(t.getValues()::remove));

        config.getContextTargets().removeIf(t -> t.getValues().isEmpty());
    }

    private void replaceTargets(EnvironmentFlagConfig config, ReplaceTargetsInstruction instruction){
        instruction.getTargets().forEach(req -> validateVariationExists(config, req.variationId()));

        config.setContextTargets(instruction.getTargets().stream()
                .map(req -> ContextTarget.builder()
                        .contextKind(req.contextKind())
                        .variation(req.variationId())
                        .values(new HashSet<>(req.values()))
                        .build())
                .collect(Collectors.toSet()));
    }

    private void validateVariationExists(EnvironmentFlagConfig config, UUID variationId){
        boolean exists = config.getFlag().getVariations().stream()
                .anyMatch(v -> v.getId().equals(variationId));

        if(!exists){
            throw new BadRequestException("Variation with ID " + variationId + " not found in flag " + config.getFlag().getKey());
        }
    }
}
