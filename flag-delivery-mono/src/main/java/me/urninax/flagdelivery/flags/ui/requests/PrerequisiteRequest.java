package me.urninax.flagdelivery.flags.ui.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;

public record PrerequisiteRequest(
        @NotEmpty(message = "key cannot be empty.")
        @JsonProperty("key")
        String key,

        @NotEmpty(message = "variation_id cannot be empty.")
        @JsonProperty("variation_id")
        String variationId
){
}
