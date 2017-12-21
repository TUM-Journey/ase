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

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableList;
import static org.apache.commons.lang3.Validate.*;

/**
 * {@code User} aggregate describes a student or tutor and holds
 * information about his or her name, roles, email, matrik. number
 * and login data.
 */
@Entity @Access(AccessType.FIELD)
@Accessors(fluent = true) @ToString
@NoArgsConstructor(force = true, access = AccessLevel.PACKAGE)
public class User {

    public static final List<UserRole> DEFAULT_USER_ROLES = asList(UserRole.STUDENT);

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
        this(email, passwordHash, DEFAULT_USER_ROLES, name, studentId);
    }

    public void addUserRole(UserRole userRole) {
        userRoles.add(notNull(userRole));
    }

    public List<UserRole> userRoles() {
        return unmodifiableList(userRoles);
    }

    public void userRoles(Collection<UserRole> userRoles) {
        if (userRoles == null || userRoles.isEmpty())
            throw new NullPointerException("Null userRoles given");

        this.userRoles.clear();
        this.userRoles.addAll(userRoles);
    }

    public boolean removeUserRole(UserRole userRole) {
        return userRoles.remove(userRole);
    }
}
