package me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.targets;

import com.fasterxml.jackson.annotation.JsonProperty;
import me.urninax.flagdelivery.flags.ui.requests.TargetInstructionRequest;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.TargetInstruction;

import java.util.List;

public class ReplaceTargetsInstruction extends TargetInstruction{
    @JsonProperty("targets")
    private List<TargetInstructionRequest> targets;
}
