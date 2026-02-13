package me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.prerequisites;

import com.fasterxml.jackson.annotation.JsonProperty;
import me.urninax.flagdelivery.flags.ui.requests.PrerequisiteRequest;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.PrerequisiteInstruction;

import java.util.List;

public class ReplacePrerequisitesInstruction extends PrerequisiteInstruction{
    @JsonProperty("prerequisites")
    List<PrerequisiteRequest> prerequisites;
}
