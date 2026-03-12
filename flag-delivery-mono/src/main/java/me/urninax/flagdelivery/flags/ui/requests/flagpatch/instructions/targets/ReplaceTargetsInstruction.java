package me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.targets;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import me.urninax.flagdelivery.flags.ui.requests.TargetInstructionRequest;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.TargetInstruction;

import java.util.List;

@Getter
public class ReplaceTargetsInstruction extends TargetInstruction{
    @NotEmpty(message = "targets cannot be empty.")
    @JsonProperty("targets")
    private List<@Valid TargetInstructionRequest> targets;
}
