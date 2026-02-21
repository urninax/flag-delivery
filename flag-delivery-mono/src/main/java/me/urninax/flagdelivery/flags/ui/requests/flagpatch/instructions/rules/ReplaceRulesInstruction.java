package me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.rules;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.RuleInstruction;
import me.urninax.flagdelivery.flags.ui.requests.rule.RuleInstructionRequest;

import java.util.List;

public class ReplaceRulesInstruction extends RuleInstruction{
    @NotEmpty(message = "rules cannot be empty.")
    @JsonProperty("rules")
    private List<@Valid RuleInstructionRequest> rules;
}
