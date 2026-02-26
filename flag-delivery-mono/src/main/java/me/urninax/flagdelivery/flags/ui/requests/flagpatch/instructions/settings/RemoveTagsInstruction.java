package me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.settings;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.SettingInstruction;

import java.util.List;

@Getter
public class RemoveTagsInstruction extends SettingInstruction{
    @NotEmpty(message = "values cannot be empty.")
    @JsonProperty("values")
    private List<String> values;
}
