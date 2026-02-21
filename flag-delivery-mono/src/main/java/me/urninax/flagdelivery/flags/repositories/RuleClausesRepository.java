package me.urninax.flagdelivery.flags.repositories;

import me.urninax.flagdelivery.flags.models.rule.RuleClause;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RuleClausesRepository extends JpaRepository<RuleClause, UUID>{
    Optional<RuleClause> findByIdAndRuleId(UUID clauseId, UUID ruleId);
}
