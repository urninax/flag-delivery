package me.urninax.flagdelivery.flags.shared;

import me.urninax.flagdelivery.flags.models.FlagVariation;

import java.util.List;

public record ResolvedVariations(
        List<FlagVariation> variations,
        int onIdx,
        int offIdx
){}
