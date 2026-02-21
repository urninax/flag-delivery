package me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.prerequisites;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import me.urninax.flagdelivery.flags.ui.requests.PrerequisiteRequest;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.PrerequisiteInstruction;

import java.util.List;

public class ReplacePrerequisitesInstruction extends PrerequisiteInstruction{
    @NotEmpty(message = "prerequisites cannot be empty.")
    @JsonProperty("prerequisites")
    private List<@Valid PrerequisiteRequest> prerequisites;
}
