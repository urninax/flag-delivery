package me.urninax.flagdelivery.flags.ui.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public record CreateFeatureFlagRequest(
        @NotBlank(message = "Feature flag name is required.")
        @Size(min = 1, max = 256, message = "Feature flag name should be 1-256 characters long.")
        @JsonProperty("name")
        String name,

        @NotBlank(message = "Feature flag key is required.")
        @Size(min = 1, max = 100, message = "Feature flag key should be 1-100 characters long.")
        @JsonProperty("key")
        String key,

        @Size(max = 1000, message = "Feature flag description should be at most 1000 characters long.")
        @JsonProperty("description")
        String description,

        @JsonProperty("variations")
        List<@Valid VariationRequest> variations,

        @JsonProperty("temporary")
        Boolean temporary,

        @Valid
        @Size(max = 20, message = "Feature flag can have max. 20 tags.")
        @JsonProperty("tags")
        Set<
                @Size(max = 64, message = "Tags should be at most 64 chars.")
                @Pattern(regexp = "^[A-Za-z0-9._-]{1,64}$", message = "Tags should contain only letters, digits, '.', '-', '_'")
                String> tags,


        @Valid
        @JsonProperty("defaults")
        VariationDefaultsRequest defaults,

        @JsonProperty("maintainer_id")
        UUID maintainerId,

        @JsonProperty("is_flag_on")
        Boolean isFlagOn
){}
