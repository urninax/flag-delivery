package me.urninax.flagdelivery.flags.ui.requests;

import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.BaseInstruction;

import java.util.List;

public record PatchFeatureFlag(
        String environmentKey,
        List<BaseInstruction> instructions
){
}
