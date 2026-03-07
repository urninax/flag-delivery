package me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.variations;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.VariationInstruction;

import java.util.UUID;

@Getter
public class UpdateVariationInstruction extends VariationInstruction{
    @NotEmpty(message = "variation_id cannot be empty.")
    @JsonProperty("variation_id")
    private UUID variationId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("value")
    private JsonNode value;

    @JsonProperty("description")
    private String description;
}
