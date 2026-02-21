package me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.rules;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.RuleInstruction;

public class RemoveRuleInstruction extends RuleInstruction{
    @NotEmpty(message = "rule_id cannot be empty.")
    @JsonProperty("rule_id")
    private String ruleId;
}
