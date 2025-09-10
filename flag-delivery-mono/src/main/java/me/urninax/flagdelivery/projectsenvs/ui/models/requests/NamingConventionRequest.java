package me.urninax.flagdelivery.projectsenvs.ui.models.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;
import me.urninax.flagdelivery.projectsenvs.models.project.CasingConvention;
import me.urninax.flagdelivery.projectsenvs.services.validation.ValidKey;

public record NamingConventionRequest(
        @JsonProperty("casing_convention")
        CasingConvention casingConvention,

        @Size(max = 128, message = "Prefix cannot be longer than 128 chars.")
        @ValidKey
        @JsonProperty("prefix")
        String prefix
){}
