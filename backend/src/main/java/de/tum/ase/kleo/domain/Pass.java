package de.tum.ase.kleo.domain;

import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.time.ZonedDateTime;

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
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "session_id", nullable = false)
    private final Session session;

    @Column(nullable = false)
    private final ZonedDateTime generatedDateTime = ZonedDateTime.now();

    @Column
    private ZonedDateTime usedDateTime;

    public Pass(String id, Session session) {
        this.id = notBlank(id);
        this.session = notNull(session);
    }

    public boolean isUsed() {
        return usedDateTime != null;
    }

    public void utilize() {
        usedDateTime = ZonedDateTime.now();
    }
}
