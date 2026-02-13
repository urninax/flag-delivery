package me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.settings;

import com.fasterxml.jackson.annotation.JsonProperty;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.SettingInstruction;

import java.util.List;

public class RemoveTagsInstruction extends SettingInstruction{
    @JsonProperty("values")
    private List<String> values;
}
