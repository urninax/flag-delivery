package me.urninax.flagdelivery.projectsenvs.ui.models.requests;

import jakarta.validation.constraints.Size;
import me.urninax.flagdelivery.projectsenvs.models.project.CasingConvention;

public record NamingConventionRequest(
        CasingConvention casingConvention,

        @Size(max = 50, message = "Prefix cannot be longer than 50 chars.")
        String prefix
){}
