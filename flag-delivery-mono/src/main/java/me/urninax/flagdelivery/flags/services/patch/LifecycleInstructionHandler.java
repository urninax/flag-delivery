package me.urninax.flagdelivery.flags.services.patch;

import lombok.RequiredArgsConstructor;
import me.urninax.flagdelivery.flags.models.FeatureFlag;
import me.urninax.flagdelivery.flags.repositories.FlagsRepository;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.LifecycleInstruction;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.lifecycle.*;
import me.urninax.flagdelivery.shared.exceptions.BadRequestException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LifecycleInstructionHandler{
    private final FlagsRepository flagsRepository;

    public void handle(FeatureFlag flag, LifecycleInstruction lifecycleInstruction){
        switch(lifecycleInstruction){
            case ArchiveFlagInstruction ignored -> archiveFlag(flag);
            case DeleteFlagInstruction ignored -> deleteFlag(flag);
            case DeprecateFlagInstruction ignored -> deprecateFlag(flag);
            case RestoreDeprecatedFlagInstruction ignored -> restoreDeprecatedFlag(flag);
            case RestoreFlagInstruction ignored -> restoreFlag(flag);
            default -> throw new BadRequestException("Unexpected lifecycle instruction");
        }
    }

    private void archiveFlag(FeatureFlag flag){
        flag.setArchived(true);
    }

    private void deleteFlag(FeatureFlag flag){
        flagsRepository.delete(flag);
    }

    private void deprecateFlag(FeatureFlag flag){
        flag.setDeprecated(true);
    }

    private void restoreDeprecatedFlag(FeatureFlag flag){
        flag.setDeprecated(false);
    }

    private void restoreFlag(FeatureFlag flag){
        flag.setArchived(false);
    }

}
