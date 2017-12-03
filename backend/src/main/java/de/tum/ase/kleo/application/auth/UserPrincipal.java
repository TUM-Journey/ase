package de.tum.ase.kleo.application.auth;

import de.tum.ase.kleo.domain.User;
import lombok.*;
import lombok.experimental.Accessors;

import static org.apache.commons.lang3.Validate.notBlank;
import static org.apache.commons.lang3.Validate.notNull;

@Getter
@Accessors(fluent = true)
@EqualsAndHashCode @ToString
@NoArgsConstructor(force = true, access = AccessLevel.PACKAGE)
public class UserPrincipal {

    private final String id;

    private final String email;

    private final String name;

    private final String studentId;

    public UserPrincipal(String id, String email, String name, String studentId) {
        this.id = notNull(id);
        this.email = notBlank(email);
        this.name = notBlank(name);
        this.studentId = studentId;
    }

    public UserPrincipal(String id, String email, String name) {
        this(id, email, name, null);
    }

    public static UserPrincipal from(User user) {
        return new UserPrincipal(user.id(), user.email(), user.name(), user.studentId());
    }
}
