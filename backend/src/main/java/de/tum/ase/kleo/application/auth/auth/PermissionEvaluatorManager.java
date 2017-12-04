package de.tum.ase.kleo.application.auth.auth;

import lombok.val;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.DenyAllPermissionEvaluator;
import org.springframework.security.core.Authentication;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PermissionEvaluatorManager implements PermissionEvaluator {

    private final static PermissionEvaluator DEFAULT_FALLBACK_PERMISSION_EVALUATOR
            = new DenyAllPermissionEvaluator();

    private final List<SelectivePermissionEvaluator> permissionEvaluators = new ArrayList<>();
    private final PermissionEvaluator fallbackPermissionEvaluator;

    public PermissionEvaluatorManager(List<SelectivePermissionEvaluator> permissionEvaluators,
                                      PermissionEvaluator fallbackPermissionEvaluator) {
        this.permissionEvaluators.addAll(permissionEvaluators);
        this.fallbackPermissionEvaluator = fallbackPermissionEvaluator;
    }

    public PermissionEvaluatorManager(List<SelectivePermissionEvaluator> permissionEvaluators) {
        this(permissionEvaluators, DEFAULT_FALLBACK_PERMISSION_EVALUATOR);
    }

    public PermissionEvaluatorManager() {
        this(Collections.emptyList(), DEFAULT_FALLBACK_PERMISSION_EVALUATOR);
    }

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {

        for (val permissionEvaluator : permissionEvaluators) {
            if (permissionEvaluator.supports(authentication, targetDomainObject, permission))
                return permissionEvaluator.hasPermission(authentication, targetDomainObject, permission);
        }

        return fallbackPermissionEvaluator.hasPermission(authentication, targetDomainObject, permission);
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        for (val permissionEvaluator : permissionEvaluators) {
            if (permissionEvaluator.supports(authentication, targetId, targetType, permission))
                return permissionEvaluator.hasPermission(authentication, targetId, targetType, permission);
        }

        return fallbackPermissionEvaluator.hasPermission(authentication, targetId, targetType, permission);
    }
}
