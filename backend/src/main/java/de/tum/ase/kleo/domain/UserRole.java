package de.tum.ase.kleo.domain;

import java.util.List;

import static java.util.stream.Collectors.toList;

public enum UserRole {
    SUPERUSER, TUTOR, STUDENT;

    public static UserRole from(String role) {
        return UserRole.valueOf(role.toUpperCase());
    }

    public static List<UserRole> from(List<String> role) {
        return role.stream().map(UserRole::from).collect(toList());
    }
}
