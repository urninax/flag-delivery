package me.urninax.flagdelivery.flags.services;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import me.urninax.flagdelivery.flags.models.EnvironmentFlagConfig;
import me.urninax.flagdelivery.flags.models.FeatureFlag;
import me.urninax.flagdelivery.flags.repositories.FlagConfigsRepository;
import me.urninax.flagdelivery.flags.utils.FlagConfigEnvironmentProjection;
import me.urninax.flagdelivery.flags.utils.exceptions.FlagConfigAlreadyExistsException;
import me.urninax.flagdelivery.projectsenvs.models.environment.Environment;
import me.urninax.flagdelivery.shared.utils.PersistenceExceptionUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FlagConfigService{
    private final FlagConfigsRepository flagConfigsRepository;
    private final EntityManager em;

    @Transactional
    public List<EnvironmentFlagConfig> createFlagConfigForEnvs(FeatureFlag flag, Set<FlagConfigEnvironmentProjection> environmentProjections){
        List<EnvironmentFlagConfig> configs = environmentProjections.stream()
                .map(proj ->
                        EnvironmentFlagConfig.builder()
                            .on(flag.isFlagOn())
                            .salt(randomHex())
                            .sel(randomHex())
                            .fallthroughVariationIdx(flag.getDefaultOnVariationIdx())
                            .offVariationIdx(flag.getDefaultOffVariationIdx())
                            .archived(false)
                            .flag(flag)
                            .environment(em.getReference(Environment.class, proj.getId()))
                            .build()
                ).toList();

        try{
            flagConfigsRepository.saveAllAndFlush(configs);
        }catch(DataIntegrityViolationException exc){
            if(PersistenceExceptionUtils.isUniqueException(exc)){
                throw new FlagConfigAlreadyExistsException();
            }
            throw exc;
        }

        return configs;
    }

    private String randomHex(){
        return UUID.randomUUID().toString().replace("-", "");
    }
}
