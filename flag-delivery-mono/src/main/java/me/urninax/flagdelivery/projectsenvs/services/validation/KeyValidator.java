package me.urninax.flagdelivery.projectsenvs.services.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import me.urninax.flagdelivery.projectsenvs.utils.ReservedWordsProperties;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class KeyValidator implements ConstraintValidator<ValidKey, String>{
    private static final String REGEX = "^[A-Za-z0-9][A-Za-z0-9._-]*$";
    private final Set<String> reserved;

    public KeyValidator(ReservedWordsProperties reservedWordsProps){
        this.reserved = new HashSet<>(reservedWordsProps.keys());
    }


    @Override
    public void initialize(ValidKey constraintAnnotation){
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext){
        if(s == null){
            return true;
        }
        Set<String> reservedKeysSet = new HashSet<>(reserved);

        boolean matchesRegex = s.matches(REGEX);
        boolean isNotReserved = !reservedKeysSet.contains(s);

        return matchesRegex && isNotReserved;
    }
}
