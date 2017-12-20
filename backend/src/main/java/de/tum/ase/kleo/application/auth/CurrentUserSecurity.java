package de.tum.ase.kleo.application.auth;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import de.tum.ase.kleo.domain.GroupRepository;
import de.tum.ase.kleo.domain.User;
import de.tum.ase.kleo.domain.id.UserId;
import lombok.val;

@Service("currentUser")
public class CurrentUserSecurity {

    private final GroupRepository groupRepository;

    @Autowired
    public CurrentUserSecurity(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    public boolean hasUserId(UserId userId) {
        val authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!authentication.isAuthenticated())
            return false;

        val userPrincipal = (User) authentication.getPrincipal();
        val userPrincipalId = userPrincipal.id();
        return userId.equals(userPrincipalId);
    }

    public boolean hasUserId(String userIdRaw) {
        return hasUserId(UserId.of(userIdRaw));
    }
}
