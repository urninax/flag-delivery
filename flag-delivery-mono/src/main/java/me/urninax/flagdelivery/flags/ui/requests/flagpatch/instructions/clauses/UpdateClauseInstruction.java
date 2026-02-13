package me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.clauses;

import com.fasterxml.jackson.annotation.JsonProperty;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.ClauseInstruction;
import me.urninax.flagdelivery.flags.ui.requests.rule.ClauseRequest;

public class UpdateClauseInstruction extends ClauseInstruction{
    @JsonProperty("clause_id")
    private String clauseId;

    @JsonProperty("clause")
    private ClauseRequest clause;
}
