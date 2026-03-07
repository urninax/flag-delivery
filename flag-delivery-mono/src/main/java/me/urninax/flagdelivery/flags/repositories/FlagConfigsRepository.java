package me.urninax.flagdelivery.flags.repositories;

import me.urninax.flagdelivery.flags.models.EnvironmentFlagConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FlagConfigsRepository extends JpaRepository<EnvironmentFlagConfig, UUID>{
    @Query(value = "SELECT f.key FROM environment_flag_config efc " +
                   "JOIN feature_flag f ON efc.flag_id = f.id " +
                   "WHERE efc.prerequisites @> CAST(:jsonQuery AS jsonb)", nativeQuery = true)
    List<String> findFlagsWithPrerequisiteOnVariation(@Param("jsonQuery") String jsonQuery);
}
