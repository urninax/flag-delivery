package me.urninax.flagdelivery.flags.shared;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DefaultsDTO(
        @JsonProperty("on_variation_idx")
        int onVariationIdx,

        @JsonProperty("off_variation_idx")
        int offVariationIdx
){
}
