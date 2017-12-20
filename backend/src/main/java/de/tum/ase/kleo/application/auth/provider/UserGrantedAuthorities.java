package de.tum.ase.kleo.application.auth.provider;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import de.tum.ase.kleo.domain.UserRole;

import static java.util.stream.Collectors.toList;

final class UserGrantedAuthorities {

    private UserGrantedAuthorities() {
        throw new AssertionError("No UserGrantedAuthorities instance for you");
    }

    public static List<GrantedAuthority> fromUserRoles(List<UserRole> userRoles) {
        return userRoles.stream()
                .map(role -> new SimpleGrantedAuthority(role.name()))
                .collect(toList());
    }
}
