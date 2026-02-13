package me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.rules;

import com.fasterxml.jackson.annotation.JsonProperty;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.RuleInstruction;
import me.urninax.flagdelivery.flags.ui.requests.rule.RuleInstructionRequest;

import java.util.List;

public class ReplaceRulesInstruction extends RuleInstruction{
    @JsonProperty("rules")
    private List<RuleInstructionRequest> rules;
}
