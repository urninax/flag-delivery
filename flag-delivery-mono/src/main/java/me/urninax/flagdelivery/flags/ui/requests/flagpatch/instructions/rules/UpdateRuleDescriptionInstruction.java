package me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.rules;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.RuleInstruction;

public class UpdateRuleDescriptionInstruction extends RuleInstruction{
    @NotEmpty(message = "description cannot be empty.")
    @JsonProperty("description")
    private String description;

    @NotEmpty(message = "rule_id cannot be empty.")
    @JsonProperty("rule_id")
    private String ruleId;
}
