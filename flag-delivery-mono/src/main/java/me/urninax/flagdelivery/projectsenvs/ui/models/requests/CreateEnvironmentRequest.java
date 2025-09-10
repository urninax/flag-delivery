package me.urninax.flagdelivery.projectsenvs.ui.models.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import me.urninax.flagdelivery.projectsenvs.services.validation.ValidKey;

import java.util.List;

public record CreateEnvironmentRequest(
        @JsonProperty("name")
        @NotBlank(message = "Environment name is required.")
        @Size(max = 256, message = "Environment name should be at most 256 chars long.")
        String name,

        @JsonProperty("key")
        @NotBlank(message = "Environment key is required.")
        @Size(max = 128, message = "Environment key should be at most 128 chars long.")
        @ValidKey
        String key,

        @JsonProperty("confirm_changes")
        boolean confirmChanges,

        @JsonProperty("require_comments")
        boolean requireComments,

        @JsonProperty("critical")
        @Valid
        List<
                @Size(max = 50, message = "Tags should be at most 50 chars.")
                @Pattern(regexp = "^[A-Za-z0-9]+$", message = "Tags should contain only letters, digits, '.', '-', '_'")
                String> tags,
        boolean critical
){}
