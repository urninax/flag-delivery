package me.urninax.flagdelivery.flags.repositories;

import me.urninax.flagdelivery.flags.models.rule.Rule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RulesRepository extends JpaRepository<Rule, UUID>{
    @Modifying
    @Query("UPDATE Rule r SET r.priority = r.priority + 1 " +
            "WHERE r.environmentFlagConfig.id = :configId AND r.priority >= :targetPriority")
    void incrementPriorities(UUID configId, int targetPriority);

    @Modifying
    @Query("UPDATE Rule r SET r.priority = r.priority - 1 " +
            "WHERE r.environmentFlagConfig.id = :configId AND r.priority > :targetPriority")
    void decrementPriorities(UUID configId, int targetPriority);
}
