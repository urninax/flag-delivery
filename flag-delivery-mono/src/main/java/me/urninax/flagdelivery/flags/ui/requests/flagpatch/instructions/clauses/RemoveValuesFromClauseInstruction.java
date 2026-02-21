package me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.clauses;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.ClauseInstruction;

import java.util.List;
import java.util.UUID;

@Getter
public class RemoveValuesFromClauseInstruction extends ClauseInstruction{
    @NotEmpty(message = "clause_id is required.")
    @JsonProperty("clause_id")
    private UUID clauseId;

    @NotEmpty(message = "values is required.")
    @JsonProperty("values")
    private List<String> values;
}
