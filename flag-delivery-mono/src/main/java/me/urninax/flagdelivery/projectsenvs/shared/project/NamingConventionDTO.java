package me.urninax.flagdelivery.projectsenvs.shared.project;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import me.urninax.flagdelivery.projectsenvs.models.project.CasingConvention;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record NamingConventionDTO(
        @JsonProperty("case")
        CasingConvention casing,
        String prefix
){}
