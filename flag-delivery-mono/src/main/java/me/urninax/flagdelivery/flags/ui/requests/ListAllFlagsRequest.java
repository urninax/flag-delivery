package me.urninax.flagdelivery.flags.ui.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record ListAllFlagsRequest(
        @Size(max = 256, message = "Query can be at most 256 characters long")
        @JsonProperty("query")
        String query,

        @JsonProperty("tags")
        @Valid
        @Size(max = 20, message = "Environment can have max. 20 tags.")
        List<
                @Size(max = 64, message = "Tags should be at most 64 chars.")
                @Pattern(regexp = "^[A-Za-z0-9._-]{1,64}$", message = "Tags should contain only letters, digits, '.', '-', '_'")
                        String> tags,

        UUID maintainer,
        String type){
}
