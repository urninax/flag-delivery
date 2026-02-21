package me.urninax.flagdelivery.flags.ui.requests.rule;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class RuleInstructionRequest{
    @NotEmpty(message = "variation_id cannot be empty.")
    @JsonProperty("variation_id")
    private String variationId;

    @JsonProperty("description")
    private String description;

    @NotEmpty(message = "clauses cannot be empty.")
    @JsonProperty("clauses")
    private List<@Valid ClauseRequest> clauses;
}
