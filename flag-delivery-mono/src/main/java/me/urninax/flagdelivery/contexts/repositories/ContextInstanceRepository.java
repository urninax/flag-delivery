package me.urninax.flagdelivery.contexts.repositories;

import me.urninax.flagdelivery.contexts.models.ContextInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContextInstanceRepository extends JpaRepository<ContextInstance, UUID>{
    Optional<ContextInstance> findByHash(String hash);
}
