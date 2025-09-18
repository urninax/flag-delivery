package me.urninax.flagdelivery.projectsenvs.ui.models.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PatchProjectRequest(
        @JsonProperty("name")
        @Size(max = 256, message = "Project name should be at most 256 chars long.")
        String name,

        @JsonProperty("tags")
        @Valid
        @Size(max = 20, message = "Project can have max. 20 tags.")
        List<
                @Size(max = 64, message = "Tags should be at most 64 chars.")
                @Pattern(regexp = "^[A-Za-z0-9._-]{1,64}$", message = "Tags should contain only letters, digits, '.', '-', '_'")
                        String> tags
){ }
