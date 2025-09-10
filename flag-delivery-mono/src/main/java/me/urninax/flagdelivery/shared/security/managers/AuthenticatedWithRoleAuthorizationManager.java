package me.urninax.flagdelivery.shared.security.managers;

import lombok.extern.slf4j.Slf4j;
import me.urninax.flagdelivery.organisation.models.membership.OrgRole;
import me.urninax.flagdelivery.shared.security.CurrentUser;
import me.urninax.flagdelivery.shared.security.enums.AuthMethod;
import me.urninax.flagdelivery.shared.utils.PermissionEvaluator;
import me.urninax.flagdelivery.shared.utils.annotations.RequiresAuthMethod;
import me.urninax.flagdelivery.shared.utils.annotations.RequiresRole;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
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

        RequiresAuthMethod authAnn = findAnnotation(method, RequiresAuthMethod.class);
        RequiresRole roleAnn = findAnnotation(method, RequiresRole.class);

        boolean authOk = authAnn == null || authAnn.value() == AuthMethod.ANY || user.isAuthMethod(authAnn.value());
        boolean roleOk = roleAnn == null || roleAnn.value() == OrgRole.NONE || perm.canAccess(roleAnn.value());

        log.info("Auth check: method={}, authOk={}, roleOk={}", method.getName(), authOk, roleOk);

        return new AuthorizationDecision(authOk && roleOk);
    }

    private <A extends Annotation> A findAnnotation(Method method, Class<A> annClass){
        if(method.isAnnotationPresent(annClass)){
            return method.getAnnotation(annClass);
        }
        Class<?> clazz = method.getDeclaringClass();
        if(clazz.isAnnotationPresent(annClass)){
            return clazz.getAnnotation(annClass);
        }

        return null;
    }
}
