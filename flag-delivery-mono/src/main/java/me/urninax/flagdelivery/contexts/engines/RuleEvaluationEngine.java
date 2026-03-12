package me.urninax.flagdelivery.contexts.engines;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import me.urninax.flagdelivery.contexts.models.ContextInstance;
import me.urninax.flagdelivery.flags.models.FlagVariation;
import me.urninax.flagdelivery.flags.models.rule.ClauseOp;
import me.urninax.flagdelivery.flags.models.rule.Rule;
import me.urninax.flagdelivery.flags.models.rule.RuleClause;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
public class RuleEvaluationEngine{
    public Optional<FlagVariation> evaluate(Rule rule, List<ContextInstance> contextInstances){
        boolean allClausesMatch = Optional.ofNullable(rule.getClauses()).stream()
                .flatMap(Collection::stream)
                .allMatch(clause -> matchClause(clause, contextInstances));

        return allClausesMatch ? Optional.ofNullable(rule.getVariation()) : Optional.empty();
    }

    private boolean matchClause(RuleClause clause, List<ContextInstance> contextInstances){
        return contextInstances.stream()
                .filter(ctx -> ctx.getBody().path("kind").asText("").equals(clause.getContextKindKey()))
                .findFirst()
                .map(ctx -> {
                    JsonNode attrValue = extractValue(ctx, clause.getAttribute());
                    if(attrValue.isMissingNode() || attrValue.isNull())
                        return false;

                    boolean match = evaluateOperator(clause.getOp(), attrValue, clause.getValues());

                    return clause.getNegate() != match;
                })
                .orElse(false);
    }

    private boolean evaluateOperator(ClauseOp operator, JsonNode attrValue, List<String> targetValues){
        String valueStr = attrValue.asText();

        return switch(operator){
            case IS_ONE_OF -> targetValues.contains(valueStr);
            case STARTS_WITH -> targetValues.stream().anyMatch(valueStr::startsWith);
            case ENDS_WITH -> targetValues.stream().anyMatch(valueStr::endsWith);
            case CONTAINS -> targetValues.stream().anyMatch(valueStr::contains);
            case MATCHES_REGEX -> targetValues.stream().anyMatch(valueStr::matches);
            case LESS_THAN -> compare(attrValue, targetValues) < 0;
            case LESS_THAN_OR_EQUAL -> compare(attrValue, targetValues) <= 0;
            case GREATER_THAN -> compare(attrValue, targetValues) > 0;
            case GREATER_THAN_OR_EQUAL -> compare(attrValue, targetValues) >= 0;
        };
    }

    private int compare(JsonNode attrValue, List<String> targetValues){
        if(targetValues.isEmpty()) return 0;

        String firstTarget = targetValues.getFirst();

        if(attrValue.isNumber()){
            try{
                double targetNum = Double.parseDouble(firstTarget);
                return Double.compare(attrValue.asDouble(), targetNum);
            }catch(NumberFormatException e){
                return 0;
            }
        }

        return attrValue.asText().compareTo(firstTarget);
    }

    private JsonNode extractValue(ContextInstance contextInstance, String attribute){
        if(attribute == null || attribute.isBlank()){
            return MissingNode.getInstance();
        }

        JsonNode body = contextInstance.getBody();

        if(attribute.startsWith("/")){
            return body.at(attribute);
        }

        return body.path(attribute);
    }
}
