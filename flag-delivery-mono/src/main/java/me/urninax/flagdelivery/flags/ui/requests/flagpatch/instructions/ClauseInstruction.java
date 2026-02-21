package me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public abstract class ClauseInstruction extends BaseInstruction{
    @NotEmpty(message = "rule_id is required.")
    @JsonProperty("rule_id")
    private UUID ruleId;

    @Override
    public boolean requiresEnvironmentKey(){
        return true;
    }
}
