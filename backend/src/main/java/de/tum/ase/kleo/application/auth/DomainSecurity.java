package de.tum.ase.kleo.application.auth;


import lombok.val;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service("currentUser")
public class DomainSecurity {

    public boolean hasUserId(String userIdRaw) {
        val authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!authentication.isAuthenticated())
            return false;

        val userPrincipal = (UserPrincipal) authentication.getPrincipal();
        val userId = userPrincipal.id();
        return userId.equals(userIdRaw);
    }
}
