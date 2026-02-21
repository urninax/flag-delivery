package me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.clauses;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.ClauseInstruction;
import me.urninax.flagdelivery.flags.ui.requests.rule.ClauseRequest;

import java.util.List;

@Getter
public class AddClausesInstruction extends ClauseInstruction{
    @NotEmpty(message = "clauses field is required.")
    @JsonProperty("clauses")
    private List<@Valid ClauseRequest> clauses;
}
