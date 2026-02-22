package me.urninax.flagdelivery.flags.shared;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record EnvironmentFlagConfigDTO(
        Boolean on,
        Boolean archived,
        String salt,
        String sel,

        @JsonProperty("off_variation_id")
        UUID offVariationId,

        @JsonProperty("fallthrough_variation_id")
        UUID fallthroughVariationId,

        @JsonProperty("updated_at")
        Instant updatedAt
){
}
