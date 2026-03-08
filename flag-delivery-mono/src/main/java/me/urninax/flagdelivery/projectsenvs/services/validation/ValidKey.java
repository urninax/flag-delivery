package me.urninax.flagdelivery.projectsenvs.services.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = KeyValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidKey{
    String message() default "Invalid key: must start with a letter or a digit, contain only letters, digits, '.', '-', '_'";

    KeyType type() default KeyType.ANY;

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
