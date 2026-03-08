package me.urninax.flagdelivery.contexts.shared;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ContextKindDTO(
    UUID id,
    String name,
    String key,

    String description,
    boolean archived,
    Long version,

    @JsonProperty("created_at")
    Instant createdAt,

    @JsonProperty("updated_at")
    Instant updatedAt,

    @JsonProperty("last_seen")
    Instant lastSeen
){}
