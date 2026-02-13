package me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.prerequisites;

import com.fasterxml.jackson.annotation.JsonProperty;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.PrerequisiteInstruction;

public class AddPrerequisiteInstruction extends PrerequisiteInstruction{
    @JsonProperty("key")
    private String key;

    @JsonProperty("variation_id")
    private String variationId;
}
