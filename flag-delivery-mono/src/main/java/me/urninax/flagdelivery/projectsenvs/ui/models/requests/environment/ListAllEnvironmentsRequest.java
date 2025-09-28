package me.urninax.flagdelivery.projectsenvs.ui.models.requests.environment;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ListAllEnvironmentsRequest(
        @Size(max = 256, message = "Query can be at most 256 characters long")
        @JsonProperty("query")
        String query,
        List<String> tags
){}
