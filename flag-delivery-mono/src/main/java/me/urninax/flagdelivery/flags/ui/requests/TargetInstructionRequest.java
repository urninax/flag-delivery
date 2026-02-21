package me.urninax.flagdelivery.flags.ui.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record TargetInstructionRequest(
        @NotEmpty(message = "context_kind is required.")
        @JsonProperty("context_kind")
        String contextKind,

        @NotEmpty(message = "variation_id is required.")
        @JsonProperty("variation_id")
        String variationId,

        @NotEmpty(message = "values cannot be empty.")
        @JsonProperty("values")
        List<String> values
){
}
