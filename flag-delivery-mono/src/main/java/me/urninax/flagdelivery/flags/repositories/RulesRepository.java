package me.urninax.flagdelivery.flags.repositories;

import me.urninax.flagdelivery.flags.models.rule.Rule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RulesRepository extends JpaRepository<Rule, UUID>{
    @Query("""
        select r from Rule r
        join fetch r.clauses
        where r.id = :ruleId
            and r.environmentFlagConfig.environment.key = :envKey
            and r.environmentFlagConfig.flag.project.id = :projectId
    """)
    Optional<Rule> findRuleWithSecurityCheck(UUID ruleId, String envKey, UUID projectId);
}
