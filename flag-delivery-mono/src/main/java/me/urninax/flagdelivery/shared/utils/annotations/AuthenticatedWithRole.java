package me.urninax.flagdelivery.shared.utils.annotations;

import me.urninax.flagdelivery.organisation.models.membership.OrgRole;
import me.urninax.flagdelivery.shared.security.enums.AuthMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthenticatedWithRole{
    AuthMethod method() default AuthMethod.ANY;
    OrgRole role() default OrgRole.NONE;
}
