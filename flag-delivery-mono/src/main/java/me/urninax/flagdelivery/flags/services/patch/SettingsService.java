package me.urninax.flagdelivery.flags.services.patch;

import me.urninax.flagdelivery.flags.models.FeatureFlag;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.SettingInstruction;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.settings.AddTagsInstruction;
import org.springframework.stereotype.Service;

@Service
public class SettingsService{
    public void handle(FeatureFlag flag, SettingInstruction settingInstruction){
        switch(settingInstruction){
            case AddTagsInstruction instruction -> flag.addTags(instruction.getTags());
            default -> throw new IllegalStateException("Unexpected value: " + settingInstruction);
        }
    }
}
