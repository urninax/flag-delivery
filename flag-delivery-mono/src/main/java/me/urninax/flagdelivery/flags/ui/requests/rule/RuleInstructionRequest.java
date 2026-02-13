package me.urninax.flagdelivery.flags.ui.requests.rule;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class RuleInstructionRequest{
    @JsonProperty("variation_id")
    private String variationId;

    @JsonProperty("description")
    private String description;

    @JsonProperty("clauses")
    private List<ClauseRequest> clauses;
}
