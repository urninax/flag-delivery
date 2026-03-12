package me.urninax.flagdelivery.flags.services.patch;

import lombok.RequiredArgsConstructor;
import me.urninax.flagdelivery.flags.models.EnvironmentFlagConfig;
import me.urninax.flagdelivery.flags.models.FeatureFlag;
import me.urninax.flagdelivery.flags.models.FlagVariation;
import me.urninax.flagdelivery.flags.models.rule.Rule;
import me.urninax.flagdelivery.flags.repositories.FlagConfigsRepository;
import me.urninax.flagdelivery.flags.services.FlagVariationsService;
import me.urninax.flagdelivery.flags.ui.requests.PatchVariationRequest;
import me.urninax.flagdelivery.flags.ui.requests.VariationRequest;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.VariationInstruction;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.variations.*;
import me.urninax.flagdelivery.flags.utils.exceptions.VariationInUseException;
import me.urninax.flagdelivery.flags.utils.exceptions.VariationNotFoundException;
import me.urninax.flagdelivery.shared.exceptions.BadRequestException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class VariationsInstructionHandler{
    private final FlagVariationsService flagVariationsService;
    private final FlagConfigsRepository flagConfigsRepository;

    public void handle(FeatureFlag flag, EnvironmentFlagConfig config, VariationInstruction variationInstruction){
        switch(variationInstruction){
            case AddVariationInstruction inst -> addVariation(flag, inst);
            case RemoveVariationInstruction inst -> removeVariation(flag, inst);
            case UpdateDefaultVariationInstruction inst -> updateDefaultVariation(flag, inst);
            case UpdateFallthroughVariationInstruction inst -> updateFallthroughVariation(flag, config, inst);
            case UpdateOffVariationInstruction inst -> updateOffVariation(flag, config, inst);
            case UpdateVariationInstruction inst -> updateVariationInstruction(flag, inst);
            default -> throw new BadRequestException("Unsupported variation instruction");
        }
    }

    private void addVariation(FeatureFlag flag, AddVariationInstruction instruction){
        VariationRequest variationRequest = VariationRequest.builder()
                .name(instruction.getName())
                .description(instruction.getDescription())
                .value(instruction.getValue())
                .build();

        flagVariationsService.createVariation(variationRequest, flag);
    }

    private void removeVariation(FeatureFlag flag, RemoveVariationInstruction instruction){
        UUID variationId = instruction.getVariationId();

        if (flag.getVariations().size() <= 1) {
            throw new VariationInUseException("Cannot remove the last variation of a flag.");
        }

        FlagVariation variationToRemove = flag.getVariations().stream()
                .filter(v -> Objects.equals(v.getId(), variationId))
                .findFirst()
                .orElseThrow(VariationNotFoundException::new);

        checkVariationUsages(flag, variationId);

        flag.getVariations().remove(variationToRemove);
    }

    private void updateDefaultVariation(FeatureFlag flag, UpdateDefaultVariationInstruction instruction){
        if(instruction.getOnVariationId() != null){
            FlagVariation variation = flag.getVariations().stream()
                    .filter(v -> Objects.equals(v.getId(), instruction.getOnVariationId()))
                    .findFirst()
                    .orElseThrow(VariationNotFoundException::new);

            flag.setDefaultOnVariation(variation);
        }

        if(instruction.getOffVariationId() != null){
            FlagVariation variation = flag.getVariations().stream()
                    .filter(v -> Objects.equals(v.getId(), instruction.getOffVariationId()))
                    .findFirst()
                    .orElseThrow(VariationNotFoundException::new);

            flag.setDefaultOffVariation(variation);
        }
    }

    private void updateFallthroughVariation(FeatureFlag flag, EnvironmentFlagConfig config, UpdateFallthroughVariationInstruction instruction){
        validateConfig(config);

        FlagVariation variation = findVariationInFlag(flag, instruction.getVariationId());

        if(variation != null){
            config.setFallthroughVariation(variation);
        }
    }

    private void updateOffVariation(FeatureFlag flag, EnvironmentFlagConfig config, UpdateOffVariationInstruction instruction){
        validateConfig(config);

        FlagVariation variation = findVariationInFlag(flag, instruction.getVariationId());

        if(variation != null){
            config.setOffVariation(variation);
        }
    }

    private void updateVariationInstruction(FeatureFlag flag, UpdateVariationInstruction instruction){
        PatchVariationRequest request = PatchVariationRequest.builder()
                .name(instruction.getName())
                .description(instruction.getDescription())
                .value(instruction.getValue())
                .build();

        flagVariationsService.updateVariation(flag, request, instruction.getVariationId());
    }

    private FlagVariation findVariationInFlag(FeatureFlag flag, UUID variationId){
        if(variationId == null) return null;

        return flag.getVariations().stream()
                .filter(v -> Objects.equals(v.getId(), variationId))
                .findFirst()
                .orElseThrow(VariationNotFoundException::new);
    }

    private void validateConfig(EnvironmentFlagConfig config){
        if(config == null) throw new BadRequestException("Environment config is required");
    }

    private UUID getIdSafe(FlagVariation variation) {
        return variation != null ? variation.getId() : null;
    }

    private void checkVariationUsages(FeatureFlag flag, UUID variationId){
        List<String> usages = new ArrayList<>();

        if (flag.getDefaultOnVariation() != null && Objects.equals(flag.getDefaultOnVariation().getId(), variationId)) {
            usages.add("Flag default 'on' variation");
        }
        if (flag.getDefaultOffVariation() != null && Objects.equals(flag.getDefaultOffVariation().getId(), variationId)) {
            usages.add("Flag default 'off' variation");
        }

        if (flag.getFlagConfigs() != null) {
            for (EnvironmentFlagConfig config : flag.getFlagConfigs()) {
                String envKey = config.getEnvironment().getKey();

                if (Objects.equals(getIdSafe(config.getOffVariation()), variationId)) {
                    usages.add(String.format("Environment '%s' off-variation", envKey));
                }
                if (Objects.equals(getIdSafe(config.getFallthroughVariation()), variationId)) {
                    usages.add(String.format("Environment '%s' fallthrough variation", envKey));
                }

                if (config.getRules() != null) {
                    for (Rule rule : config.getRules()) {
                        if (Objects.equals(getIdSafe(rule.getVariation()), variationId)) {
                            usages.add(String.format("Environment '%s' Rule '%s'", envKey, rule.getId()));
                        }
                    }
                }
            }
        }

        String jsonQuery = String.format("[{\"variation\": \"%s\"}]", variationId);
        List<String> dependentFlags = flagConfigsRepository.findFlagsWithPrerequisiteOnVariation(jsonQuery);
        for (String otherFlagKey : dependentFlags) {
            usages.add(String.format("Prerequisite for flag '%s'", otherFlagKey));
        }

        if (!usages.isEmpty()) {
            String message = "Variation is in use and cannot be removed: " + String.join(", ", usages);
            throw new VariationInUseException(message);
        }
    }
}
