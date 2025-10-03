package me.urninax.flagdelivery.flags.repositories;

import me.urninax.flagdelivery.flags.models.EnvironmentFlagConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FlagConfigsRepository extends JpaRepository<EnvironmentFlagConfig, UUID>{
}
