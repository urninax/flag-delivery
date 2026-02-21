package me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.rules;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.RuleInstruction;

public class UpdateRuleVariationInstruction extends RuleInstruction{
    @NotEmpty(message = "rule_id cannot be empty.")
    @JsonProperty("rule_id")
    private String ruleId;

    @NotEmpty(message = "variation_id cannot be empty.")
    @JsonProperty("variation_id")
    private String variationId;
}
