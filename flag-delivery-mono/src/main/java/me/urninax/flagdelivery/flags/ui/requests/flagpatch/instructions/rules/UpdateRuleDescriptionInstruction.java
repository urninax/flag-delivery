package me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.rules;

import com.fasterxml.jackson.annotation.JsonProperty;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.RuleInstruction;

public class UpdateRuleDescriptionInstruction extends RuleInstruction{
    @JsonProperty("description")
    private String description;

    @JsonProperty("rule_id")
    private String ruleId;
}
