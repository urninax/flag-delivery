package me.urninax.flagdelivery.projectsenvs.ui.models.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import me.urninax.flagdelivery.projectsenvs.services.validation.ValidKey;

import java.util.List;

public record CreateProjectRequest(
        @JsonProperty("name")
        @NotBlank(message = "Project name is required.")
        @Size(max = 256, message = "Project name should be at most 256 chars long.")
        String name,

        @JsonProperty("key")
        @NotBlank(message = "Project key is required.")
        @Size(max = 128, message = "Project key should be at most 128 chars long.")
        @ValidKey
        String key,

        @JsonProperty("tags")
        @Valid
        @Size(max = 20, message = "Project can have max. 20 tags.")
        List<
                @Size(max = 64, message = "Tags should be at most 64 chars.")
                @Pattern(regexp = "^[A-Za-z0-9._-]{1,64}$", message = "Tags should contain only letters, digits, '.', '-', '_'")
                String> tags,
//        List<CreateEnvironmentRequest> environments,

        @JsonProperty("naming_convention")
        NamingConventionRequest namingConvention
){}
