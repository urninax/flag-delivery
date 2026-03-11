package me.urninax.flagdelivery.contexts.ui.requests.validation;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import me.urninax.flagdelivery.contexts.ui.requests.EvaluationContextRequest;
import me.urninax.flagdelivery.projectsenvs.services.validation.KeyType;
import me.urninax.flagdelivery.projectsenvs.utils.ReservedWordsProperties;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
public class EvaluationContextRequestValidator implements ConstraintValidator<ValidEvaluationContext, EvaluationContextRequest>{

    private static final String KEY_REGEX = "^[A-Za-z0-9][A-Za-z0-9._-]*$";
    private static final int MAX_DEPTH = 10;
    private static final int MAX_ATTRIBUTES = 300;

    private final ReservedWordsProperties reservedWordsProperties;

    @Override
    public boolean isValid(EvaluationContextRequest value, ConstraintValidatorContext context){
        if(value == null) return true;

        List<String> reservedKinds = reservedWordsProperties.types()
                .getOrDefault(KeyType.CONTEXTKIND, Collections.emptyList());

        if("multi".equalsIgnoreCase(value.getKind())){
            if(value.getAttributes().isEmpty()){
                addViolation(context, "Multi-context must contain at least one nested context");
                return false;
            }

            for(Map.Entry<String, JsonNode> entry : value.getAttributes().entrySet()){
                String subKind = entry.getKey();
                JsonNode subNode = entry.getValue();

                if(!isValidKey(subKind, reservedKinds, context)) return false;

                if(!subNode.isObject() || subNode.path("key").isMissingNode() || subNode.path("key").asText().isBlank()){
                    addViolation(context, "Nested context for '" + subKind + "' must be an object with a 'key'");
                    return false;
                }
            }
        }else{
            if(value.getKey() == null || value.getKey().isBlank()){
                addViolation(context, "Context key cannot be empty for single context");
                return false;
            }
        }

        return validateComplexity(value.getAttributes(), context);
    }

    private boolean isValidKey(String key, List<String> reserved, ConstraintValidatorContext context){
        if(!key.matches(KEY_REGEX)){
            addViolation(context, "Context kind '" + key + "' contains invalid characters");
            return false;
        }
        if(reserved.contains(key)){
            addViolation(context, "Context kind '" + key + "' is a reserved word");
            return false;
        }
        return true;
    }

    private boolean validateComplexity(Map<String, JsonNode> attributes, ConstraintValidatorContext context){
        AtomicInteger count = new AtomicInteger(0);
        for(JsonNode node : attributes.values()){
            if(!validateNode(node, 1, count)){
                addViolation(context, "Context exceeds complexity limits (max depth " + MAX_DEPTH + " or max attributes " + MAX_ATTRIBUTES + ")");
                return false;
            }
        }
        return true;
    }

    private boolean validateNode(JsonNode node, int depth, AtomicInteger count){
        if(depth > MAX_DEPTH || count.incrementAndGet() > MAX_ATTRIBUTES) return false;
        if(node.isObject()){
            for(JsonNode child : node){
                if(!validateNode(child, depth + 1, count)) return false;
            }
        }else if(node.isArray()){
            for(JsonNode item : node){
                if(!validateNode(item, depth + 1, count)) return false;
            }
        }
        return true;
    }

    private void addViolation(ConstraintValidatorContext context, String message){
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }
}
