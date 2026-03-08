package me.urninax.flagdelivery.contexts.repositories;

import me.urninax.flagdelivery.contexts.models.ContextKind;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContextKindRepository extends JpaRepository<ContextKind, UUID>{
    Optional<ContextKind> findByProjectIdAndKey(UUID projectId, String key);
}
