package me.urninax.flagdelivery.projectsenvs.services.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = KeyValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidKey{
    String message() default "Invalid key: must start with a letter or a digit, contain only letters, digits, '.', '-', '_'";

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
