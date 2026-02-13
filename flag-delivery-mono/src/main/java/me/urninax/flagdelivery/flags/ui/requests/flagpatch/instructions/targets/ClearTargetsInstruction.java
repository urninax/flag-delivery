package me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.targets;

import com.fasterxml.jackson.annotation.JsonProperty;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.TargetInstruction;

public class ClearTargetsInstruction extends TargetInstruction{
    @JsonProperty("variation_id")
    private String variationId;
}
