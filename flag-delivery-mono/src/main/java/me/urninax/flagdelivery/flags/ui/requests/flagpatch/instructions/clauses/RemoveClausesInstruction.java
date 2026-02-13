package me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.clauses;

import com.fasterxml.jackson.annotation.JsonProperty;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.ClauseInstruction;

import java.util.List;

public class RemoveClausesInstruction extends ClauseInstruction{
    @JsonProperty("clause_ids")
    private List<String> clauseIds;
}
