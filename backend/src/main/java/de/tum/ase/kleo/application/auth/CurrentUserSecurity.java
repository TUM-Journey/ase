package de.tum.ase.kleo.application.auth;


import de.tum.ase.kleo.domain.GroupRepository;
import de.tum.ase.kleo.domain.id.GroupId;
import de.tum.ase.kleo.domain.id.UserId;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service("currentUser")
public class CurrentUserSecurity {

    private final GroupRepository groupRepository;

    @Autowired
    public CurrentUserSecurity(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    public boolean hasUserId(String userIdRaw) {
        val authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!authentication.isAuthenticated())
            return false;

        val userPrincipal = (UserPrincipal) authentication.getPrincipal();
        val userId = userPrincipal.getId();
        return userId.equals(userIdRaw);
    }
}
