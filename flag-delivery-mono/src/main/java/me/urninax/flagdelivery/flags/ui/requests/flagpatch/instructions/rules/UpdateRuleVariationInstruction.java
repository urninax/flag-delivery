package me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.rules;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.RuleInstruction;

import java.util.UUID;

@Getter
public class UpdateRuleVariationInstruction extends RuleInstruction{
    @NotEmpty(message = "rule_id cannot be empty.")
    @JsonProperty("rule_id")
    private UUID ruleId;

    @NotEmpty(message = "variation_id cannot be empty.")
    @JsonProperty("variation_id")
    private UUID variationId;
}
