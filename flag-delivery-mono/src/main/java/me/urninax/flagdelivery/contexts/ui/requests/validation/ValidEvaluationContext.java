package me.urninax.flagdelivery.contexts.ui.requests.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EvaluationContextRequestValidator.class)
@Documented
public @interface ValidEvaluationContext {
    String message() default "Invalid evaluation context structure or keys";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
