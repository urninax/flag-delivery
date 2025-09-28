package me.urninax.flagdelivery.flags.ui.requests;

import jakarta.validation.constraints.Min;

public record VariationDefaultsRequest(
        @Min(value = 0, message = "Default ON variation index cannot be less than 0")
        Integer onVariation,

        @Min(value = 0, message = "Default OFF variation index cannot be less than 0")
        Integer offVariation
){
}
