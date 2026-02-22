package me.urninax.flagdelivery.flags.repositories;

import me.urninax.flagdelivery.flags.models.FeatureFlag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FlagsRepository extends JpaRepository<FeatureFlag, UUID>, FlagsRepositoryCustom{
    Optional<FeatureFlag> findByProjectIdAndKey(UUID projectId, String flagKey);

    @Query("""
        select f from FeatureFlag f
        left join fetch f.flagConfigs c
        left join fetch f.variations v
        left join fetch c.rules r
        left join fetch r.clauses
        where f.project.id = :projectId
            and f.key = :flagKey
            and c.environment.key = :envKey
    """)
    Optional<FeatureFlag> findByProjectIdAndKeyWithConfig(UUID projectId, String flagKey, String envKey);
    boolean deleteByProjectIdAndKey(UUID projectId, String flagKey);
}
