package me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.rules;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.RuleInstruction;

import java.util.List;

public class ReorderRulesInstruction extends RuleInstruction{
    @NotEmpty(message = "rule_ids cannot be empty.")
    @JsonProperty("rule_ids")
    private List<String> ruleIds;
}
