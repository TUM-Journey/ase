package de.tum.ase.kleo.application.auth;


import de.tum.ase.kleo.domain.GroupRepository;
import de.tum.ase.kleo.domain.id.GroupId;
import de.tum.ase.kleo.domain.id.UserId;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service("currentUser")
public class DomainSecurity {

    private final GroupRepository groupRepository;

    @Autowired
    public DomainSecurity(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    public boolean isTutorOf(String groupIdRaw) {
        val authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!authentication.isAuthenticated())
            return false;

        val userPrincipal = (UserPrincipal) authentication.getPrincipal();
        val userId = UserId.of(userPrincipal.id());
        val groupId = GroupId.of(groupIdRaw);

        return groupRepository.findOptionalByIdAndTutorIdsContaining(groupId, userId).isPresent();
    }

    public boolean hasUserId(String userIdRaw) {
        val authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!authentication.isAuthenticated())
            return false;

        val userPrincipal = (UserPrincipal) authentication.getPrincipal();
        val userId = userPrincipal.id();
        return userId.equals(userIdRaw);
    }
}
