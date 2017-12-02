package de.tum.ase.kleo.domain;

import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.time.LocalDateTime;

import static org.apache.commons.lang3.Validate.notBlank;
import static org.apache.commons.lang3.Validate.notNull;

@Entity
@Accessors(fluent = true)
@Getter @ToString @EqualsAndHashCode(of = "id")
@NoArgsConstructor(force = true, access = AccessLevel.PACKAGE)
public class Pass {

    @Id
    @Column(name = "pass_id")
    private final String id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private final User user;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "session_id", nullable = false)
    private final Session session;

    @Column(nullable = false)
    private final LocalDateTime generatedDateTime = LocalDateTime.now();

    @Column
    private LocalDateTime usedDateTime;

    public Pass(String id, User user, Session session) {
        this.id = notBlank(id);
        this.user = notNull(user);
        this.session = notNull(session);
    }

    public boolean isUsed() {
        return usedDateTime != null;
    }

    public void utilize() {
        usedDateTime = LocalDateTime.now();
    }
}
