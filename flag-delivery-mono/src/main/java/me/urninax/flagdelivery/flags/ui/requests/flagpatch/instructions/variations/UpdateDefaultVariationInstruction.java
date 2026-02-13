package me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.variations;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.VariationInstruction;

public class UpdateDefaultVariationInstruction extends VariationInstruction{
    @JsonProperty("on_variation_value")
    private JsonNode onVariationValue;

    @JsonProperty("off_variation_value")
    private JsonNode offVariationValue;
}
