package me.urninax.flagdelivery.projectsenvs.services.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import me.urninax.flagdelivery.projectsenvs.utils.ReservedWordsProperties;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class KeyValidator implements ConstraintValidator<ValidKey, String>{
    private static final String REGEX = "^[A-Za-z0-9][A-Za-z0-9._-]*$";
    private final Map<KeyType, Set<String>> reservedMap;
    private KeyType currentType;

    public KeyValidator(ReservedWordsProperties reservedWordsProps){
        if (reservedWordsProps.types() == null) {
            this.reservedMap = Collections.emptyMap();
        } else {
            this.reservedMap = reservedWordsProps.types().entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            e -> new HashSet<>(e.getValue())
                    ));
        }
    }

    @Override
    public void initialize(ValidKey constraintAnnotation){
        this.currentType = constraintAnnotation.type();
    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext context){
        if(s == null){
            return true;
        }

        if (!s.matches(REGEX)) {
            return false; // Uses the default message from annotation
        }

        if (currentType == KeyType.ANY) {
            return true;
        }

        Set<String> reserved = reservedMap.getOrDefault(currentType, Collections.emptySet());
        if (reserved.contains(s)) {
            context.disableDefaultConstraintViolation();

            String bannedWordsList = String.join(", ", reserved);
            String message = String.format("Key '%s' is reserved. Banned words for %s: [%s]",
                                          s, currentType.getName(), bannedWordsList);

            context.buildConstraintViolationWithTemplate(message)
                   .addConstraintViolation();
            return false;
        }
        
        return true;
    }
}
