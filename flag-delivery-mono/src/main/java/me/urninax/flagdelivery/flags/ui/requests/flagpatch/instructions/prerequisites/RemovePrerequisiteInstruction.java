package me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.prerequisites;

import com.fasterxml.jackson.annotation.JsonProperty;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.PrerequisiteInstruction;

public class RemovePrerequisiteInstruction extends PrerequisiteInstruction{
    @JsonProperty("key")
    private String key;
}
