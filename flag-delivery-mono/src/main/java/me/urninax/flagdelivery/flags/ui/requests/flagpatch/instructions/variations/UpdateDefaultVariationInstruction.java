package me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.variations;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.VariationInstruction;

import java.util.UUID;

@Getter
public class UpdateDefaultVariationInstruction extends VariationInstruction{
    @JsonProperty("on_variation_id")
    private UUID onVariationId;

    @JsonProperty("off_variation_id")
    private UUID offVariationId;
}
