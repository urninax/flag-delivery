package me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.clauses;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.ClauseInstruction;

import java.util.List;
import java.util.UUID;

@Getter
public class RemoveClausesInstruction extends ClauseInstruction{
    @NotEmpty(message = "clause_ids is required.")
    @JsonProperty("clause_ids")
    private List<UUID> clauseIds;
}
