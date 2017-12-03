package de.tum.ase.kleo.domain;

import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.Validate.notNull;

@Embeddable
@Accessors(fluent = true)
@Getter @ToString @EqualsAndHashCode(of = "session") // One pass per session
@NoArgsConstructor(force = true, access = AccessLevel.PACKAGE)
public class Pass {

    @Column(name = "code")
    private final String code;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "session_id", nullable = false)
    private final Session session;

    @Column(nullable = false)
    private final ZonedDateTime generatedDateTime = ZonedDateTime.now();

    @Column
    private ZonedDateTime usedDateTime;

    public Pass(String code, Session session) {
        this.code = isNotBlank(code) ? code : UUID.randomUUID().toString();
        this.session = notNull(session);
    }

    public Pass(Session session) {
        this(null, session);
    }

    public boolean isUsed() {
        return usedDateTime != null;
    }

    public void utilize() {
        usedDateTime = ZonedDateTime.now();
    }
}
