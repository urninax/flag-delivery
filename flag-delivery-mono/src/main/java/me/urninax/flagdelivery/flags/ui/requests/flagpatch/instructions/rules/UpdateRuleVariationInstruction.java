package me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.rules;

import com.fasterxml.jackson.annotation.JsonProperty;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.RuleInstruction;

public class UpdateRuleVariationInstruction extends RuleInstruction{
    @JsonProperty("rule_id")
    private String ruleId;

    @JsonProperty("variation_id")
    private String variationId;
}
