package de.tum.ase.kleo.domain;


import de.tum.ase.kleo.domain.id.UserId;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.util.Collection;
import java.util.List;

import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableList;
import static org.apache.commons.lang3.Validate.*;

@Entity @Access(AccessType.FIELD)
@Accessors(fluent = true) @ToString
@NoArgsConstructor(force = true, access = AccessLevel.PACKAGE)
public class User {

    public static final UserRole DEFAULT_USER_ROLE = UserRole.USER;

    @Getter
    @EmbeddedId
    private final UserId id = new UserId();

    @Getter
    @Column(nullable = false)
    private final String email;

    @Getter
    @Column(nullable = false)
    private final String passwordHash;

    @Enumerated(EnumType.STRING)
    @ElementCollection(fetch = FetchType.EAGER)
    @Column(name = "user_roles", nullable = false)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    private final List<UserRole> userRoles;

    @Getter
    @Column(nullable = false)
    private final String name;

    @Getter
    @Column
    private final String studentId;

    public User(String email, String passwordHash, List<UserRole> userRoles, String name, String studentId) {
        this.email = notBlank(email);
        this.passwordHash = notBlank(passwordHash);
        this.userRoles = notEmpty(userRoles);
        this.name = notBlank(name);
        this.studentId = studentId;
    }

    public User(String email, String passwordHash, String name, String studentId) {
        this(email, passwordHash, singletonList(DEFAULT_USER_ROLE), name, studentId);
    }

    public void addUserRole(UserRole userRole) {
        userRoles.add(notNull(userRole));
    }

    public List<UserRole> userRoles() {
        return unmodifiableList(userRoles);
    }

    public void userRoles(Collection<UserRole> userRoles) {
        truncateUserRoles();
        this.userRoles.addAll(userRoles);
    }

    public boolean removeUserRole(UserRole userRole) {
        return userRoles.remove(userRole);
    }

    public void truncateUserRoles() {
        userRoles.clear();
    }
}
