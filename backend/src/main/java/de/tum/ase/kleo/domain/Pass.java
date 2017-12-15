package de.tum.ase.kleo.domain;

import de.tum.ase.kleo.domain.id.SessionId;
import de.tum.ase.kleo.domain.id.UserId;
import lombok.*;
import lombok.experimental.Accessors;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.apache.commons.lang3.Validate.notNull;

@Embeddable
@ToString @EqualsAndHashCode
@Getter @Accessors(fluent = true)
@NoArgsConstructor(force = true, access = AccessLevel.PACKAGE)
public class Pass implements Serializable {

    private final static Duration DEFAULT_EXPIRE = Duration.ofMinutes(15);

    @Column(nullable = false)
    private final UUID code = UUID.randomUUID();

    @Column(nullable = false)
    @AttributeOverride(name = "id", column = @Column(name = "session_id"))
    private final SessionId sessionId;

    @Column(nullable = false)
    @AttributeOverride(name = "id", column = @Column(name = "student_id"))
    private final UserId studentId;

    @Column(name = "requested_at", nullable = false)
    private final OffsetDateTime requestedAt = OffsetDateTime.now();

    @Column(name = "expires_at", nullable = false)
    private final OffsetDateTime expiresAt;

    public Pass(SessionId sessionId, UserId studentId, Duration expireIn) {
        this.sessionId = notNull(sessionId);
        this.studentId = notNull(studentId);
        this.expiresAt = requestedAt.plus(expireIn);
    }

    public Pass(SessionId sessionId, UserId studentId) {
        this(sessionId, studentId, DEFAULT_EXPIRE);
    }

    public boolean isExpired() {
        return OffsetDateTime.now().isAfter(expiresAt);
    }

    public boolean notExpired() {
        return !isExpired();
    }
}
