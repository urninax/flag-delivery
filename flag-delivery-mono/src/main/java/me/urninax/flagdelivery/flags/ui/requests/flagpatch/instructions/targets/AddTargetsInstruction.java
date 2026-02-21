package me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.targets;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.TargetInstruction;

import java.util.List;

public class AddTargetsInstruction extends TargetInstruction{
    @NotEmpty(message = "variation_id cannot be empty.")
    @JsonProperty("variation_id")
    private String variationId;

    @NotEmpty(message = "values cannot be empty.")
    @JsonProperty("values")
    private List<String> values;
}
