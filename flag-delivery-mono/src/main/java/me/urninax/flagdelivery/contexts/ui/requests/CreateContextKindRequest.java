package me.urninax.flagdelivery.contexts.ui.requests;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import org.openapitools.jackson.nullable.JsonNullable;

public record CreateContextKindRequest(
        @Size(min = 2, max = 64, message = "Context kind name should be 2-64 characters.")
        @NotEmpty(message = "Context kind name cannot be empty.")
        String name,

        @Size(max = 512, message = "Context kind description should be at most 512 characters.")
        JsonNullable<String> description
){
}
