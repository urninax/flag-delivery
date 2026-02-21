package me.urninax.flagdelivery.flags.ui.requests.rule;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import me.urninax.flagdelivery.flags.models.rule.ClauseOp;

import java.util.List;

public record ClauseRequest(
        @NotEmpty(message = "context_kind is required.")
        String contextKind,

        @NotEmpty(message = "attribute is required.")
        String attribute,

        @NotNull(message = "op is required.")
        ClauseOp op,

        @NotNull(message = "negate is required")
        boolean negate,

        @NotEmpty(message = "values cannot be empty.")
        List<@Size(max = 200, message = "Value cannot be longer than 200 chars") String> values
){
}
