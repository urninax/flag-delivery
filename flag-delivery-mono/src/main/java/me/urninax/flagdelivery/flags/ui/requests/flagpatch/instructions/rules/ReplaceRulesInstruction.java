package me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.rules;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.RuleInstruction;
import me.urninax.flagdelivery.flags.ui.requests.rule.RuleRequest;

import java.util.List;

@Getter
public class ReplaceRulesInstruction extends RuleInstruction{
    @NotEmpty(message = "rules cannot be empty.")
    @JsonProperty("rules")
    private List<@Valid RuleRequest> rules;
}
