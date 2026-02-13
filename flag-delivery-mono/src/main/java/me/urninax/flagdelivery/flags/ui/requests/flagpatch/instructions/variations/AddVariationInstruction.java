package me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.variations;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.VariationInstruction;

public class AddVariationInstruction extends VariationInstruction{
    @JsonProperty("name")
    private String name;

    @JsonProperty("value")
    private JsonNode value;

    @JsonProperty("description")
    private String description;
}
