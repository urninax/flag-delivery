package me.urninax.flagdelivery.flags.shared;

import com.fasterxml.jackson.annotation.JsonProperty;
import me.urninax.flagdelivery.flags.models.FlagKind;
import me.urninax.flagdelivery.user.shared.UserDTO;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public record FeatureFlagDTO(
        UUID id,
        String name,
        FlagKind kind,
        String description,
        String key,
        List<FlagVariationDTO> variations,
        DefaultsDTO defaults,
        Boolean temporary,

        @JsonProperty("flag_on")
        Boolean flagOn,

        @JsonProperty("maintainer_id")
        UUID maintainerId,
        UserDTO maintainer,
        Boolean archived,
        Set<String> tags,

        @JsonProperty("created_at")
        Instant createdAt,

        @JsonProperty("updated_at")
        Instant updatedAt,
        Map<String, EnvironmentFlagConfigDTO> environments
){}
