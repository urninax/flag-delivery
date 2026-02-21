package me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.prerequisites;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.PrerequisiteInstruction;

public class AddPrerequisiteInstruction extends PrerequisiteInstruction{
    @NotEmpty(message = "key cannot be empty.")
    @JsonProperty("key")
    private String key;

    @NotEmpty(message = "variation_id cannot be empty.")
    @JsonProperty("variation_id")
    private String variationId;
}
