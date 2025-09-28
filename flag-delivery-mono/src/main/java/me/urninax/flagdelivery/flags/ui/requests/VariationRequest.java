package me.urninax.flagdelivery.flags.ui.requests;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record VariationRequest(
        @Size(max = 256, message = "Variation name should be at most 256 characters.")
        String name,

        @NotNull(message = "Variation value is required.")
        JsonNode value,

        @Size(max = 2000, message = "Variation description should be at most 2000 characters")
        String description
){
}
