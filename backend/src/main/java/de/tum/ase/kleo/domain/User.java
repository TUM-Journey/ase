package de.tum.ase.kleo.domain;

import lombok.*;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static java.util.Collections.*;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.Validate.*;

@Entity
@Access(AccessType.FIELD)
@Accessors(fluent = true)
@Getter @ToString @EqualsAndHashCode(of = "id")
@NoArgsConstructor(force = true, access = AccessLevel.PACKAGE)
public class User {

    public static final UserRole DEFAULT_USER_ROLE = UserRole.STUDENT;

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

    @ElementCollection
    @CollectionTable(name = "user_passes", joinColumns = @JoinColumn(name = "user_id"))
    private Set<Pass> passes;

    public User(String id, List<UserRole> userRoles, String email, String passwordHash,
                String name, String studentId, Set<Pass> passes) {
        this.id = isNotBlank(id) ? id : UUID.randomUUID().toString();
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
        this(null, userRoles, email, passwordHash, name, studentId, passes);
    }

    public User(List<UserRole> userRoles, String email, String passwordHash,
                String name, String studentId) {
        this(null, userRoles, email, passwordHash, name, studentId, emptySet());
    }

    public User(String email, String passwordHash, String name, String studentId, Set<Pass> passes) {
        this(singletonList(DEFAULT_USER_ROLE), email, passwordHash, name, studentId, passes);
    }

    public Set<Pass> passes() {
        return unmodifiableSet(passes);
    }

    public Optional<Pass> pass(String sessionId) {
        return passes.stream().filter(pass -> pass.session().id().equals(sessionId)).findAny();
    }

    public Optional<Pass> pass(Session session) {
        return pass(session.id());
    }

    public Set<Pass> usedPasses() {
        return passes.stream().filter(Pass::isUsed).collect(toSet());
    }

    public void addPass(Pass pass) {
        passes.add(notNull(pass));
    }

    public boolean removePass(Pass pass) {
        return passes.remove(pass);
    }

    public boolean removePass(String passCode) {
        return passes.removeIf(pass -> pass.code().equals(passCode));
    }
}
