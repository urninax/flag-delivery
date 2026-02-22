package me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.rules;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.RuleInstruction;

import java.util.List;
import java.util.UUID;

@Getter
public class ReorderRulesInstruction extends RuleInstruction{
    @NotEmpty(message = "rule_ids cannot be empty.")
    @JsonProperty("rule_ids")
    private List<@NotEmpty(message = "rule_id cannot be empty") UUID> ruleIds;
}
