package me.urninax.flagdelivery.flags.services.patch;

import lombok.RequiredArgsConstructor;
import me.urninax.flagdelivery.flags.models.rule.Rule;
import me.urninax.flagdelivery.flags.models.rule.RuleClause;
import me.urninax.flagdelivery.flags.repositories.RuleClausesRepository;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.ClauseInstruction;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.clauses.*;
import me.urninax.flagdelivery.flags.utils.exceptions.rule.ClauseNotFoundException;
import me.urninax.flagdelivery.shared.utils.EntityMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ClausesService{
    private final EntityMapper entityMapper;
    private final RuleClausesRepository ruleClausesRepository;

    @Transactional
    public void handle(Rule rule, ClauseInstruction clauseInstruction){
        switch(clauseInstruction){
            case AddClausesInstruction instruction -> addClauses(rule, instruction);
            case AddValuesToClauseInstruction instruction -> addValuesToClause(rule, instruction);
            case RemoveClausesInstruction instruction -> removeClauses(rule, instruction);
            case RemoveValuesFromClauseInstruction instruction -> removeValuesFromClause(rule, instruction);
            case UpdateClauseInstruction instruction -> updateClause(rule, instruction);
            default -> throw new IllegalStateException("Unexpected value: " + clauseInstruction); //todo: change
        }
    }

    private void addClauses(Rule rule, AddClausesInstruction instruction){
        List<RuleClause> newClauses = instruction.getClauses().stream()
                .map(entityMapper::toEntity)
                .toList();

        newClauses.forEach(rule::addClause);
    }

    private void addValuesToClause(Rule rule, AddValuesToClauseInstruction instruction){
        RuleClause clause = ruleClausesRepository.findByIdAndRuleId(instruction.getClauseId(), rule.getId())
                .orElseThrow(ClauseNotFoundException::new);

        List<String> combined = Stream.concat(clause.getValues().stream(), instruction.getValues().stream())
                .distinct()
                .toList();

        clause.setValues(combined);
    }

    private void removeClauses(Rule rule, RemoveClausesInstruction instruction){
        rule.getClauses().removeIf(clause ->
                instruction.getClauseIds().contains(clause.getId()));
    }

    private void removeValuesFromClause(Rule rule, RemoveValuesFromClauseInstruction instruction){
        RuleClause clause = ruleClausesRepository.findByIdAndRuleId(instruction.getClauseId(), rule.getId())
                .orElseThrow(ClauseNotFoundException::new);

        Set<String> updatedValues = new LinkedHashSet<>(clause.getValues());
        instruction.getValues().forEach(updatedValues::remove);

        clause.setValues(new ArrayList<>(updatedValues));
    }

    private void updateClause(Rule rule, UpdateClauseInstruction instruction){
        RuleClause clause = ruleClausesRepository.findByIdAndRuleId(instruction.getClauseId(), rule.getId())
                .orElseThrow(ClauseNotFoundException::new);
        
        entityMapper.updateEntityFromRequest(instruction.getClause(), clause);
    }
}
