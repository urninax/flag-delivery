package me.urninax.flagdelivery.flags.ui.requests;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PrerequisiteRequest(
        @JsonProperty("key")
        String key,

        @JsonProperty("variation_id")
        String variationId
){
}
