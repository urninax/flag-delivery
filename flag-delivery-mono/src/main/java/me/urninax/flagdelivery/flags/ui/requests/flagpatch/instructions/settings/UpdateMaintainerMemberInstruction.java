package me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.settings;

import com.fasterxml.jackson.annotation.JsonProperty;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.SettingInstruction;

public class UpdateMaintainerMemberInstruction extends SettingInstruction{
    @JsonProperty("value")
    private String value;
}
