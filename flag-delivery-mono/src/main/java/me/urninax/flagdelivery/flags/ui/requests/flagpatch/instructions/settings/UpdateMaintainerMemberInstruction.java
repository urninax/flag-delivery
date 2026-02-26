package me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.settings;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.SettingInstruction;

import java.util.UUID;

@Getter
public class UpdateMaintainerMemberInstruction extends SettingInstruction{
    @NotEmpty(message = "value cannot be empty.")
    @JsonProperty("value")
    private UUID value;
}
