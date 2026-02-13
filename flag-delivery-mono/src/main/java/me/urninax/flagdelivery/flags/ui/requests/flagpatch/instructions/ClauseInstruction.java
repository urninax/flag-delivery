package me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class ClauseInstruction extends BaseInstruction{
    @JsonProperty("rule_id")
    private String ruleId;
}
