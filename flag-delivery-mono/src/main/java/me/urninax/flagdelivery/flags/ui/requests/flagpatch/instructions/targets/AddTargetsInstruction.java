package me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.targets;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.TargetInstruction;

import java.util.List;
import java.util.UUID;

@Getter
public class AddTargetsInstruction extends TargetInstruction{
    @NotEmpty(message = "variation_id cannot be empty.")
    @JsonProperty("variation_id")
    private UUID variationId;

    @NotEmpty(message = "context_kind cannot be empty.")
    @JsonProperty("context_kind")
    private String contextKind;

    @NotEmpty(message = "values cannot be empty.")
    @JsonProperty("values")
    private List<String> values;
}
