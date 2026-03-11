package me.urninax.flagdelivery.contexts.repositories;

import me.urninax.flagdelivery.contexts.models.Context;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContextRepository extends JpaRepository<Context, UUID>{
    Optional<Context> findByEnvironmentIdAndKey(UUID environmentId, String key);
}
