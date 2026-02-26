package me.urninax.flagdelivery.flags.services.patch;

import lombok.RequiredArgsConstructor;
import me.urninax.flagdelivery.flags.models.EnvironmentFlagConfig;
import me.urninax.flagdelivery.flags.models.FeatureFlag;
import me.urninax.flagdelivery.flags.models.Prerequisite;
import me.urninax.flagdelivery.flags.services.PrerequisitesService;
import me.urninax.flagdelivery.flags.ui.requests.PrerequisiteRequest;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.PrerequisiteInstruction;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.prerequisites.AddPrerequisiteInstruction;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.prerequisites.RemovePrerequisiteInstruction;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.prerequisites.ReplacePrerequisitesInstruction;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.prerequisites.UpdatePrerequisiteInstruction;
import me.urninax.flagdelivery.shared.exceptions.BadRequestException;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PrerequisitesInstructionHandler{
    private final PrerequisitesService prerequisitesService;

    public void handle(FeatureFlag flag, EnvironmentFlagConfig config, PrerequisiteInstruction prerequisiteInstruction){
        switch(prerequisiteInstruction){
            case AddPrerequisiteInstruction inst -> addPrerequisite(flag, config, inst);
            case RemovePrerequisiteInstruction inst -> removePrerequisite(config, inst);
            case ReplacePrerequisitesInstruction inst -> replacePrerequisites(flag, config, inst);
            case UpdatePrerequisiteInstruction inst -> updatePrerequisite(flag, config, inst);
            default -> throw new BadRequestException("Unsupported prerequisite instruction");
        }
    }

    private void addPrerequisite(FeatureFlag flag, EnvironmentFlagConfig config, AddPrerequisiteInstruction inst){
        PrerequisiteRequest request = new PrerequisiteRequest(inst.getKey(), inst.getVariationId());

        Set<Prerequisite> resolved = prerequisitesService.resolvePrerequisites(
                List.of(request),
                flag.getKey(),
                flag.getProject().getId()
        );

        prerequisitesService.validateNoConflicts(resolved, config.getPrerequisites());

        Set<Prerequisite> updatedSet = new HashSet<>(config.getPrerequisites());
        updatedSet.addAll(resolved);

        config.setPrerequisites(updatedSet);
    }

    private void removePrerequisite(EnvironmentFlagConfig config, RemovePrerequisiteInstruction inst){
        Set<Prerequisite> updatedSet = prerequisitesService.removePrerequisite(
                config.getPrerequisites(),
                inst.getKey()
        );

        config.setPrerequisites(updatedSet);
    }

    private void replacePrerequisites(FeatureFlag flag, EnvironmentFlagConfig config, ReplacePrerequisitesInstruction inst){
        Set<Prerequisite> resolved = prerequisitesService.resolvePrerequisites(
                inst.getPrerequisites(),
                flag.getKey(),
                flag.getProject().getId()
        );

        config.setPrerequisites(resolved);
    }

    private void updatePrerequisite(FeatureFlag flag, EnvironmentFlagConfig config, UpdatePrerequisiteInstruction inst){
        boolean exists = config.getPrerequisites().stream()
                .anyMatch(p -> p.getKey().equals(inst.getKey()));

        if (!exists) {
            throw new BadRequestException("Prerequisite with key " + inst.getKey() + " not found");
        }

        Set<Prerequisite> resolved = prerequisitesService.resolvePrerequisites(
                List.of(new PrerequisiteRequest(inst.getKey(), inst.getVariationId())),
                flag.getKey(),
                flag.getProject().getId()
        );

        Set<Prerequisite> updated = config.getPrerequisites().stream()
                .filter(p -> !p.getKey().equals(inst.getKey()))
                .collect(Collectors.toSet());

        updated.addAll(resolved);

        config.setPrerequisites(updated);
    }
}
