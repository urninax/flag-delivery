package me.urninax.flagdelivery.flags.repositories;

import lombok.RequiredArgsConstructor;
import me.urninax.flagdelivery.flags.models.FeatureFlag;
import me.urninax.flagdelivery.flags.utils.exceptions.FlagAlreadyExistsException;
import me.urninax.flagdelivery.flags.utils.exceptions.FlagNotFoundException;
import me.urninax.flagdelivery.shared.utils.PersistenceExceptionUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FeatureFlagPersistenceManager{
    private final FlagsRepository flagsRepository;

    public FeatureFlag saveSafely(FeatureFlag flag){
        try {
            return flagsRepository.saveAndFlush(flag);
        } catch (DataIntegrityViolationException exc) {
            if (PersistenceExceptionUtils.isUniqueException(exc)) {
                throw new FlagAlreadyExistsException();
            }
            throw exc;
        }
    }

    public void deleteSafely(UUID projectId, String flagKey) {
        if (!flagsRepository.deleteByProjectIdAndKey(projectId, flagKey)) {
            throw new FlagNotFoundException();
        }
    }
}
