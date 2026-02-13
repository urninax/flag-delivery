package me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.rules;

import com.fasterxml.jackson.annotation.JsonProperty;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.RuleInstruction;
import me.urninax.flagdelivery.flags.ui.requests.rule.ClauseRequest;

import java.util.List;

public class AddRuleInstruction extends RuleInstruction{
    @JsonProperty("variation_id")
    private String variationId;

    @JsonProperty("clauses")
    private List<ClauseRequest> clauses;

    @JsonProperty("before_rule_id")
    private String beforeRuleId;
}
