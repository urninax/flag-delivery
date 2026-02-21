package me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.variations;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.VariationInstruction;

public class UpdateOffVariationInstruction extends VariationInstruction{
    @NotEmpty(message = "variation_id cannot be empty.")
    @JsonProperty("variation_id")
    private String variationId;
}
