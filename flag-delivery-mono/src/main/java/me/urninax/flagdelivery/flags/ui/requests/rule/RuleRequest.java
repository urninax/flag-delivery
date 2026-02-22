package me.urninax.flagdelivery.flags.ui.requests.rule;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
public class RuleRequest{
    @NotEmpty(message = "variation_id cannot be empty.")
    @JsonProperty("variation_id")
    private UUID variationId;

    @JsonProperty("description")
    @Size(max = 500, message = "description cannot be longer than 500 characters.")
    private String description;

    @NotEmpty(message = "clauses cannot be empty.")
    @JsonProperty("clauses")
    private List<@Valid ClauseRequest> clauses;
}
