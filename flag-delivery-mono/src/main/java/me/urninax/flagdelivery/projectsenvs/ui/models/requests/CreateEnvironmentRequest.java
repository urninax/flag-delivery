package me.urninax.flagdelivery.projectsenvs.ui.models.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateEnvironmentRequest(
        @NotBlank(message = "Environment name is required.")
        @Size(min = 2, max = 100, message = "Environment name should be 2 to 100 chars long.")
        String name,

        @NotBlank(message = "Environment key is required.")
        @Size(min = 2, max = 20, message = "Environment key should be 2 to 20 chars long.")
        @Pattern(regexp = "^[A-Za-z0-9._-]+", message = "Environment key must only contain letters, numbers, '.', '_' and '-'")
        String key,

        boolean confirmChanges,
        boolean requireComments,

        @Valid
        List<
                @Size(max = 50, message = "Tags should be at most 50 chars.")
                String> tags,
        boolean critical
){}
