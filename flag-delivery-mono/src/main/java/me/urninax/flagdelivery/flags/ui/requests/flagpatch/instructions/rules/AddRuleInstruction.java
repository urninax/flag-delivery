package me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.rules;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.RuleInstruction;
import me.urninax.flagdelivery.flags.ui.requests.rule.ClauseRequest;

import java.util.List;
import java.util.UUID;

@Getter
public class AddRuleInstruction extends RuleInstruction{
    @NotEmpty(message = "variation_id cannot be empty.")
    @JsonProperty("variation_id")
    private UUID variationId;

    @NotEmpty(message = "clauses cannot be empty.")
    @JsonProperty("clauses")
    private List<@Valid ClauseRequest> clauses;

    @JsonProperty("before_rule_id")
    private UUID beforeRuleId;
}
