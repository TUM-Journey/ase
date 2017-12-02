package de.tum.ase.kleo.domain;

import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.Validate.notBlank;
import static org.apache.commons.lang3.Validate.notEmpty;

@Entity
@Accessors(fluent = true)
@Getter @ToString @EqualsAndHashCode(of = "id")
@NoArgsConstructor(force = true, access = AccessLevel.PACKAGE)
public class User {

    private static final UserRole DEFAULT_USER_ROLE = UserRole.STUDENT;

    @Id
    private final String id;

    @ElementCollection(fetch = FetchType.EAGER)
    @Column(name = "user_roles", nullable = false)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    private final List<UserRole> userRoles;

    @Column(nullable = false)
    private final String email;

    @Column(nullable = false)
    private final String passwordHash;

    @Column(nullable = false)
    private final String name;

    @Column
    private final String studentId;

    public User(String id, List<UserRole> userRoles, String email, String passwordHash,
                String name, String studentId) {
        this.id = notBlank(id);
        this.userRoles = notEmpty(userRoles);
        this.email = notBlank(email);
        this.passwordHash = notBlank(passwordHash);
        this.name = notBlank(name);
        this.studentId = studentId;
    }

    public User(String id, String email, String passwordHash, String name, String studentId) {
        this(id, singletonList(DEFAULT_USER_ROLE), email, passwordHash, name, studentId);
    }
}
