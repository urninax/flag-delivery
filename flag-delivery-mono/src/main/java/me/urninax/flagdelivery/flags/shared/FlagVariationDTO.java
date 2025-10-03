package me.urninax.flagdelivery.flags.shared;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.UUID;

public record FlagVariationDTO(
        UUID id,
        JsonNode value,
        String name
){
}
