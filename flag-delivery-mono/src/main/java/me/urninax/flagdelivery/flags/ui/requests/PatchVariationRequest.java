package me.urninax.flagdelivery.flags.ui.requests;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record PatchVariationRequest(
    @Size(max = 256, message = "Variation name should be at most 256 characters.")
    String name,

    JsonNode value,

    @Size(max = 2000, message = "Variation description should be at most 2000 characters")
    String description
){}
