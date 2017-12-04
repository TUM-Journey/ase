package de.tum.ase.kleo.application.auth.auth;

import de.tum.ase.kleo.application.auth.UserPrincipal;
import lombok.val;
import org.springframework.security.core.Authentication;

import java.io.Serializable;

public class UserPassesSelectivePermissionEvaluator implements SelectivePermissionEvaluator {

    private final static String TARGET_TYPE_SUPPORTED = "USER";
    private final static String PERMISSION_SUPPORTED = "passes";

    @Override
    public boolean hasPermission(Authentication authentication, Serializable userIdToCheck, String targetType, Object permission) {
        val userPrincipal = (UserPrincipal)authentication.getPrincipal();
        val userId = userPrincipal.id();
        return userId.equals(userIdToCheck);
    }

    @Override
    public boolean supports(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        if (!(permission instanceof String) || !(targetId instanceof String))
            return false;

        return TARGET_TYPE_SUPPORTED.equalsIgnoreCase(targetType)
                && PERMISSION_SUPPORTED.equalsIgnoreCase((String)permission);
    }
}
