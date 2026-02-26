package me.urninax.flagdelivery.flags.services.patch;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import me.urninax.flagdelivery.flags.models.FeatureFlag;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.SettingInstruction;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.settings.*;
import me.urninax.flagdelivery.organisation.repositories.MembershipsRepository;
import me.urninax.flagdelivery.shared.exceptions.BadRequestException;
import me.urninax.flagdelivery.shared.security.CurrentUser;
import me.urninax.flagdelivery.user.models.UserEntity;
import me.urninax.flagdelivery.user.utils.exceptions.UserNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SettingsService{
    private final EntityManager em;
    private final CurrentUser currentUser;
    private final MembershipsRepository membershipsRepository;

    public void handle(FeatureFlag flag, SettingInstruction settingInstruction){
        switch(settingInstruction){
            case AddTagsInstruction instruction -> addTags(flag, instruction);
            case MakeFlagPermanentInstruction ignored -> makeFlagPermanent(flag);
            case MakeFlagTemporaryInstruction ignored -> makeFlagTemporary(flag);
            case RemoveMaintainerInstruction ignored -> removeMaintainer(flag);
            case RemoveTagsInstruction instruction -> removeTags(flag, instruction);
            case UpdateDescriptionInstruction instruction -> updateDescription(flag, instruction);
            case UpdateMaintainerMemberInstruction instruction -> updateMaintainerMember(flag, instruction);
            default -> throw new BadRequestException("Unsupported setting instruction");
        }
    }

    private void addTags(FeatureFlag flag, AddTagsInstruction instruction){
        flag.addTags(instruction.getTags());
    }

    private void makeFlagPermanent(FeatureFlag flag){
        flag.setTemporary(false);
    }

    private void makeFlagTemporary(FeatureFlag flag){
        flag.setTemporary(true);
    }

    private void removeMaintainer(FeatureFlag flag){
        flag.setMaintainer(null);
    }

    private void removeTags(FeatureFlag flag, RemoveTagsInstruction instruction){
        instruction.getValues().forEach(flag::removeTag);
    }

    private void updateDescription(FeatureFlag flag, UpdateDescriptionInstruction instruction){
        flag.setDescription(instruction.getValue());
    }

    private void updateMaintainerMember(FeatureFlag flag, UpdateMaintainerMemberInstruction instruction){
        UUID orgId = currentUser.getOrganisationId();
        UUID newMaintainerId = instruction.getValue();

        boolean userExists = membershipsRepository.existsByUserIdAndOrganisation_Id(newMaintainerId, orgId);

        if(!userExists){
            throw new UserNotFoundException();
        }

        flag.setMaintainer(em.getReference(UserEntity.class, newMaintainerId));
    }
}
