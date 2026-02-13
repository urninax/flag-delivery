package me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.rules;

import com.fasterxml.jackson.annotation.JsonProperty;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.RuleInstruction;

import java.util.List;

public class ReorderRulesInstruction extends RuleInstruction{
    @JsonProperty("rule_ids")
    private List<String> ruleIds;
}
