package me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.targets;

import com.fasterxml.jackson.annotation.JsonProperty;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.TargetInstruction;

import java.util.List;

public class RemoveTargetsInstruction extends TargetInstruction{
    @JsonProperty("variation_id")
    private String variationId;

    @JsonProperty("values")
    private List<String> values;
}
