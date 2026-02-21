package me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.settings;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.SettingInstruction;

public class UpdateDescriptionInstruction extends SettingInstruction{
    @NotEmpty(message = "value cannot be empty.")
    @JsonProperty("value")
    private String value;
}
