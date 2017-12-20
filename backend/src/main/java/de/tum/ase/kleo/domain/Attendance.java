package de.tum.ase.kleo.domain;

import de.tum.ase.kleo.domain.id.SessionId;
import de.tum.ase.kleo.domain.id.UserId;
import lombok.*;
import lombok.experimental.Accessors;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.apache.commons.lang3.Validate.notNull;

@Embeddable
@ToString @EqualsAndHashCode
@Getter @Accessors(fluent = true)
@NoArgsConstructor(force = true, access = AccessLevel.PACKAGE)
public class Attendance implements Serializable {

    @Column(nullable = false)
    @AttributeOverride(name = "id", column = @Column(name = "session_id"))
    private final SessionId sessionId;

    @Column(nullable = false)
    @AttributeOverride(name = "id", column = @Column(name = "student_id"))
    private final UserId studentId;

    @Column(name = "attended_at", nullable = false)
    private final OffsetDateTime attendedAt = OffsetDateTime.now();

    public Attendance(SessionId sessionId, UserId studentId) {
        this.sessionId = notNull(sessionId);
        this.studentId = notNull(studentId);
    }
}
