package me.urninax.flagdelivery.projectsenvs.ui.models.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateProjectRequest(
        @NotBlank(message = "Project name is required.")
        @Size(max = 100, message = "Project name should be at most 100 chars long.")
        String name,

        @NotBlank(message = "Project key is required.")
        @Size(max = 20, message = "Project key should be at most 20 chars long.")
        @Pattern(regexp = "^[A-Za-z0-9._-]+", message = "Project key must only contain letters, numbers, '.', '_' and '-'")
        String key,

        @Valid
        List<
                @Size(max = 50, message = "Tags should be at most 50 chars.")
                String> tags,
        List<CreateEnvironmentRequest> environments,
        NamingConventionRequest namingConvention
){}
