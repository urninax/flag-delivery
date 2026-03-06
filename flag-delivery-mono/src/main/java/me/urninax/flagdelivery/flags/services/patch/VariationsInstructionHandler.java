package me.urninax.flagdelivery.flags.services.patch;

import lombok.RequiredArgsConstructor;
import me.urninax.flagdelivery.flags.models.FeatureFlag;
import me.urninax.flagdelivery.flags.models.FlagVariation;
import me.urninax.flagdelivery.flags.services.FlagVariationsService;
import me.urninax.flagdelivery.flags.ui.requests.VariationRequest;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.VariationInstruction;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.variations.AddVariationInstruction;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VariationsInstructionHandler{
    private final FlagVariationsService flagVariationsService;

    public void handle(FeatureFlag flag, VariationInstruction variationInstruction){
        switch(variationInstruction){
            case AddVariationInstruction inst -> addVariation(flag, inst);
        }
    }

    private void addVariation(FeatureFlag flag, AddVariationInstruction instruction){
        VariationRequest variationRequest = VariationRequest.builder()
                .name(instruction.getName())
                .description(instruction.getDescription())
                .value(instruction.getValue())
                .build();

        FlagVariation variation =
    }
}
