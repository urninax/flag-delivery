package me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.rules;

import com.fasterxml.jackson.annotation.JsonProperty;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.RuleInstruction;

public class RemoveRuleInstruction extends RuleInstruction{
    @JsonProperty("rule_id")
    private String ruleId;
}
