package de.tum.ase.kleo.domain;


import de.tum.ase.kleo.domain.id.UserId;
import eu.socialedge.ddd.domain.AggregateRoot;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.util.List;

import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.Validate.*;

@Entity @Access(AccessType.FIELD)
@Accessors(fluent = true) @ToString
@NoArgsConstructor(force = true, access = AccessLevel.PACKAGE)
public class User extends AggregateRoot<UserId> {

    public static final UserRole DEFAULT_USER_ROLE = UserRole.USER;

    @ElementCollection(fetch = FetchType.EAGER)
    @Column(name = "user_roles", nullable = false)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    private final List<UserRole> userRoles;

    @Getter
    @Column(nullable = false)
    private final String email;

    @Getter
    @Column(nullable = false)
    private final String passwordHash;

    @Getter
    @Column(nullable = false)
    private final String name;

    @Getter
    @Column
    private final String studentId;

    public User(UserId id, List<UserRole> userRoles, String email, String passwordHash, String name, String studentId) {
        super(nonNull(id) ? id : new UserId());
        this.userRoles = notEmpty(userRoles);
        this.email = notBlank(email);
        this.passwordHash = notBlank(passwordHash);
        this.name = notBlank(name);
        this.studentId = studentId;
    }

    public User(UserId id, String email, String passwordHash, String name, String studentId) {
        this(id, singletonList(DEFAULT_USER_ROLE), email, passwordHash, name, studentId);
    }

    public User(List<UserRole> userRoles, String email, String passwordHash,
                String name, String studentId) {
        this(null, userRoles, email, passwordHash, name, studentId);
    }

    public User(String email, String passwordHash, String name, String studentId) {
        this(singletonList(DEFAULT_USER_ROLE), email, passwordHash, name, studentId);
    }

    public List<UserRole> userRoles() {
        return unmodifiableList(userRoles);
    }

    public void addUserRole(UserRole userRole) {
        userRoles.add(notNull(userRole));
    }

    public boolean removeUserRole(UserRole userRole) {
        return userRoles.remove(userRole);
    }

    public void truncateUserRoles() {
        userRoles.clear();
    }
}
