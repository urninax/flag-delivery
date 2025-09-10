package me.urninax.flagdelivery.shared.utils.annotations;

import me.urninax.flagdelivery.organisation.models.membership.OrgRole;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresRole{
    OrgRole value() default OrgRole.NONE;
}
