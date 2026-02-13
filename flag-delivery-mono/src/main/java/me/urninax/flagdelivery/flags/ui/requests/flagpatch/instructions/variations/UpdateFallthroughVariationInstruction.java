package me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.variations;

import com.fasterxml.jackson.annotation.JsonProperty;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.VariationInstruction;

public class UpdateFallthroughVariationInstruction extends VariationInstruction{
    @JsonProperty("variation_id")
    private String variationId;
}
