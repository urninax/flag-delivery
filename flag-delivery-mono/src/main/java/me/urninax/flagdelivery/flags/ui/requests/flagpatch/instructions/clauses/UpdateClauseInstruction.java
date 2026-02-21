package me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.clauses;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.ClauseInstruction;
import me.urninax.flagdelivery.flags.ui.requests.rule.ClauseRequest;

import java.util.UUID;

@Getter
public class UpdateClauseInstruction extends ClauseInstruction{
    @NotEmpty(message = "clause_id is required.")
    @JsonProperty("clause_id")
    private UUID clauseId;

    @Valid
    @NotNull(message = "clause is required.")
    @JsonProperty("clause")
    private ClauseRequest clause;
}
