package me.urninax.flagdelivery.flags.ui.requests;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record TargetInstructionRequest(
        @JsonProperty("context_kind")
        String contextKind,

        @JsonProperty("variation_id")
        String variationId,

        @JsonProperty("values")
        List<String> values
){
}
