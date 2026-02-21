package me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.clauses;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.ClauseInstruction;

import java.util.List;
import java.util.UUID;

@Getter
public class AddValuesToClauseInstruction extends ClauseInstruction{
    @NotEmpty(message = "clause_id is required.")
    @JsonProperty("clause_id")
    private UUID clauseId;

    @NotEmpty(message = "values field is required.")
    @JsonProperty("values")
    private List<@Size(max = 200, message = "Value cannot be longer than 200 chars") String> values;
}
