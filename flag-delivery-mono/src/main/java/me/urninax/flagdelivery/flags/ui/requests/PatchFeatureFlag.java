package me.urninax.flagdelivery.flags.ui.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.BaseInstruction;

import java.util.List;

public record PatchFeatureFlag(
        String environmentKey,

        @NotEmpty(message = "instructions cannot be empty.")
        List<@Valid BaseInstruction> instructions
){
}
