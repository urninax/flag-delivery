package me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.clauses;

import com.fasterxml.jackson.annotation.JsonProperty;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.ClauseInstruction;

import java.util.List;

public class AddValuesToClauseInstruction extends ClauseInstruction{
    @JsonProperty("clause_id")
    private String clauseId;

    @JsonProperty("values")
    private List<String> values;
}
