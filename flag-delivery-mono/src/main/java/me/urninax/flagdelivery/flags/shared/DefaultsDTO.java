package me.urninax.flagdelivery.flags.shared;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DefaultsDTO(
//        @JsonProperty("on_variation_idx")
//        Integer onVariationIdx,
//
//        @JsonProperty("off_variation_idx")
//        Integer offVariationIdx,

        @JsonProperty("on_variation_id")
        UUID onVariationId,

        @JsonProperty("off_variation_id")
        UUID offVariationId
){
}
