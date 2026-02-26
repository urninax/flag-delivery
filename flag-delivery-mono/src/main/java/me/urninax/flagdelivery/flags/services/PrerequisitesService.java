package me.urninax.flagdelivery.flags.services;

import lombok.RequiredArgsConstructor;
import me.urninax.flagdelivery.flags.models.FeatureFlag;
import me.urninax.flagdelivery.flags.models.Prerequisite;
import me.urninax.flagdelivery.flags.repositories.FlagsRepository;
import me.urninax.flagdelivery.flags.ui.requests.PrerequisiteRequest;
import me.urninax.flagdelivery.shared.exceptions.BadRequestException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PrerequisitesService{
    private final FlagsRepository flagsRepository;

    public Set<Prerequisite> resolvePrerequisites(List<PrerequisiteRequest> requests,
                                     String currentFlagKey,
                                     UUID projectId){
        if (requests == null || requests.isEmpty()) {
            return new HashSet<>();
        }

        Map<String, UUID> reqMap = new HashMap<>();

        for(PrerequisiteRequest req : requests){
            if(reqMap.put(req.key(), req.variationId()) != null){
                throw new BadRequestException(String.format("Duplicate flag key in request: %s", req.key()));
            }
        }

        if(reqMap.containsKey(currentFlagKey)){
            throw new BadRequestException("A flag cannot be a prerequisite for itself.");
        }

        Set<FeatureFlag> flags = flagsRepository.findAllByKeysAndProjectIdWithVariations(
                reqMap.keySet(), projectId);

        validateKeysFound(reqMap.keySet(), flags);

        return flags.stream().map(flag -> {
            UUID requestedVariationId = reqMap.get(flag.getKey());
            validateVariationExists(flag, requestedVariationId);

            return Prerequisite.builder()
                    .key(flag.getKey())
                    .variation(requestedVariationId)
                    .build();
        }).collect(Collectors.toSet());
    }

    public Set<Prerequisite> removePrerequisite(Set<Prerequisite> current, String keyToRemove){
        if (current == null || current.isEmpty()) {
            return new HashSet<>();
        }

        Set<Prerequisite> updated = current.stream()
                .filter(p -> !p.getKey().equals(keyToRemove))
                .collect(Collectors.toSet());

        if (updated.size() == current.size()) {
            throw new BadRequestException("Prerequisite not found for key: " + keyToRemove);
        }

        return updated;
    }

    private void validateKeysFound(Set<String> requested, Set<FeatureFlag> found) {
        if (found.size() != requested.size()) {
            Set<String> foundKeys = found.stream().map(FeatureFlag::getKey).collect(Collectors.toSet());
            List<String> missing = requested.stream().filter(k -> !foundKeys.contains(k)).toList();
            throw new BadRequestException("Flags not found: " + String.join(", ", missing));
        }
    }

    public void validateNoConflicts(Set<Prerequisite> newPrereqs, Set<Prerequisite> existing) {
        if (existing == null || existing.isEmpty()) return;

        Set<String> existingKeys = existing.stream()
                .map(Prerequisite::getKey)
                .collect(Collectors.toSet());

        for (Prerequisite p : newPrereqs) {
            if (existingKeys.contains(p.getKey())) {
                throw new BadRequestException("Prerequisite already exists for key: " + p.getKey());
            }
        }
    }

    private void validateVariationExists(FeatureFlag flag, UUID variationId) {
        boolean exists = flag.getVariations().stream()
                .anyMatch(v -> v.getId().equals(variationId));
        if (!exists) {
            throw new BadRequestException(String.format("Variation %s not found for flag %s", variationId, flag.getKey()));
        }
    }
}
