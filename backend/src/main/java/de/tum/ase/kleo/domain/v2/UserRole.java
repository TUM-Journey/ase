package de.tum.ase.kleo.domain.v2;

import java.util.List;

import static java.util.stream.Collectors.toList;

public enum UserRole {
    SUPERUSER, USER;

    public static UserRole from(String role) {
        return UserRole.valueOf(role.toUpperCase());
    }

    public static List<UserRole> from(List<String> role) {
        return role.stream().map(UserRole::from).collect(toList());
    }
}
