package me.urninax.flagdelivery.shared.security.managers;

import lombok.extern.slf4j.Slf4j;
import me.urninax.flagdelivery.organisation.models.membership.OrgRole;
import me.urninax.flagdelivery.shared.security.CurrentUser;
import me.urninax.flagdelivery.shared.security.enums.AuthMethod;
import me.urninax.flagdelivery.shared.utils.PermissionEvaluator;
import me.urninax.flagdelivery.shared.utils.annotations.AuthenticatedWithRole;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.function.Supplier;

@Slf4j
@Component
public class AuthenticatedWithRoleAuthorizationManager implements AuthorizationManager<MethodInvocation>{

    private final CurrentUser user;
    private final PermissionEvaluator perm;

    @Autowired
    public AuthenticatedWithRoleAuthorizationManager(CurrentUser user, PermissionEvaluator perm){
        this.user = user;
        this.perm = perm;
    }

    @Override
    public void verify(Supplier<Authentication> authentication, MethodInvocation object){
        AuthorizationManager.super.verify(authentication, object);
    }

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, MethodInvocation object){
        return null;
    }

    @Override
    public AuthorizationResult authorize(Supplier<Authentication> authentication, MethodInvocation invocation){
        Method method = invocation.getMethod();
        log.info("INVOKED");
        AuthenticatedWithRole ann = findAnnotation(method);
        if(ann == null){
            return new AuthorizationDecision(true);
        }

        boolean authOk = ann.method() == AuthMethod.ANY || user.isAuthMethod(ann.method());
        boolean roleOk = ann.role() == OrgRole.NONE || perm.canAccess(ann.role());

        return new AuthorizationDecision(authOk && roleOk);
    }

    private AuthenticatedWithRole findAnnotation(Method method){
        if(method.isAnnotationPresent(AuthenticatedWithRole.class)){
            return method.getAnnotation(AuthenticatedWithRole.class);
        }
        Class<?> clazz = method.getDeclaringClass();
        if(clazz.isAnnotationPresent(AuthenticatedWithRole.class)){
            return clazz.getAnnotation(AuthenticatedWithRole.class);
        }

        return null;
    }
}
