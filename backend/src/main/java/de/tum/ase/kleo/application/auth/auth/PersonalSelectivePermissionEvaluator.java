package de.tum.ase.kleo.application.auth.auth;

import de.tum.ase.kleo.application.auth.UserPrincipal;
import lombok.val;
import org.springframework.security.core.Authentication;

import java.io.Serializable;

/**
 * {@code PersonalSelectivePermissionEvaluator} validates if the given userId equals to
 * currently logged in user (depending on token store in can be database, jwt token etc).
 * <p>
 * <b>Example:</b>
 * <pre>
 * {@literal @}PreAuthorize("hasPermission(#userId, 'user', 'current')")
 *  public List getStudentGroups(String userId) {
 *       // Ensures the permission will be granted iff
 *       // userId === authentication.getPrincipal().id()
 *  }
 * </pre>
 */
public class PersonalSelectivePermissionEvaluator implements SelectivePermissionEvaluator {

    private final static String TARGET_TYPE_SUPPORTED = "user";
    private final static String PERMISSION_SUPPORTED = "current";

    @Override
    public boolean hasPermission(Authentication authentication, Serializable userIdToCheck, String targetType, Object permission) {
        if (!authentication.isAuthenticated())
            return false;

        val userPrincipal = (UserPrincipal) authentication.getPrincipal();
        val userId = userPrincipal.id();
        return userId.equals(userIdToCheck);
    }

    @Override
    public boolean supports(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        if (!(permission instanceof String) || !(targetId instanceof String))
            return false;

        return TARGET_TYPE_SUPPORTED.equalsIgnoreCase(targetType)
                && PERMISSION_SUPPORTED.equalsIgnoreCase((String) permission);
    }
}
