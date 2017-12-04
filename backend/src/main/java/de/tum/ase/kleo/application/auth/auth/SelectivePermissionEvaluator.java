package de.tum.ase.kleo.application.auth.auth;

import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;

import java.io.Serializable;

public interface SelectivePermissionEvaluator extends PermissionEvaluator {

    @Override
    default boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        return false;
    }

    @Override
    default boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        return false;
    }

    default boolean supports(Authentication authentication, Object targetDomainObject, Object permission) {
        return false;
    }

    default boolean supports(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        return false;
    }
}
