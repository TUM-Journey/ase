package de.tum.ase.kleo.domain;

import lombok.*;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static java.util.Collections.*;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.Validate.*;

@Entity
@Accessors(fluent = true)
@Getter @ToString @EqualsAndHashCode(of = "id")
@NoArgsConstructor(force = true, access = AccessLevel.PACKAGE)
public class User {

    private static final UserRole DEFAULT_USER_ROLE = UserRole.STUDENT;

    @Id
    @Column(name = "user_id")
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

    @OneToMany(cascade = CascadeType.REMOVE)
    @JoinColumn(name="user_id", referencedColumnName="user_id")
    private Set<Pass> passes;

    public User(String id, List<UserRole> userRoles, String email, String passwordHash,
                String name, String studentId, Set<Pass> passes) {
        this.id = notBlank(id);
        this.userRoles = notEmpty(userRoles);
        this.email = notBlank(email);
        this.passwordHash = notBlank(passwordHash);
        this.name = notBlank(name);
        this.studentId = studentId;
        this.passes = notNull(passes);
    }

    public User(String id, String email, String passwordHash,
                String name, String studentId, Set<Pass> passes) {
        this(id, singletonList(DEFAULT_USER_ROLE), email, passwordHash, name, studentId, passes);
    }

    public User(String id, List<UserRole> userRoles, String email, String passwordHash,
                String name, String studentId) {
        this(id, userRoles, email, passwordHash, name, studentId, emptySet());
    }

    public User(String id, String email, String passwordHash, String name, String studentId) {
        this(id, singletonList(DEFAULT_USER_ROLE), email, passwordHash, name, studentId);
    }

    public User(List<UserRole> userRoles, String email, String passwordHash,
                String name, String studentId, Set<Pass> passes) {
        this(UUID.randomUUID().toString(), userRoles, email, passwordHash, name, studentId, passes);
    }

    public User(String email, String passwordHash, String name, String studentId, Set<Pass> passes) {
        this(singletonList(DEFAULT_USER_ROLE), email, passwordHash, name, studentId, passes);
    }

    public Set<Pass> passes() {
        return unmodifiableSet(passes);
    }

    public Set<Pass> usedPasses() {
        return passes.stream().filter(Pass::isUsed).collect(toSet());
    }

    public void addPass(Pass pass) {
        passes.add(notNull(pass));
    }

    public void removePass(Pass pass) {
        passes.remove(pass);
    }

    public void removePass(String passId) {
        passes.removeIf(pass -> pass.id().equals(passId));
    }
}
