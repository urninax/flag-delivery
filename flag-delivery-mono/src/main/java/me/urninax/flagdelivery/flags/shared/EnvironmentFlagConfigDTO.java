package me.urninax.flagdelivery.flags.shared;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record EnvironmentFlagConfigDTO(
        Boolean on,
        Boolean archived,
        String salt,
        String sel,

        @JsonProperty("off_variation_idx")
        int offVariationIdx,

        @JsonProperty("fallthrough_variation_idx")
        int fallthroughVariationIdx,

        @JsonProperty("updated_at")
        Instant updatedAt
){
}
