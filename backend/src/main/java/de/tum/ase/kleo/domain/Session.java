package de.tum.ase.kleo.domain;

import de.tum.ase.kleo.domain.id.SessionId;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.Validate;

import javax.persistence.*;
import java.time.OffsetDateTime;

import static org.apache.commons.lang3.Validate.notBlank;
import static org.apache.commons.lang3.Validate.notNull;

/**
 * {@code Session} entity describe one occurrence of the group’s
 * tutorials or exercises students can visit. It consists of session type,
 * location and begin & end date times.
 */
@Entity @Access(AccessType.FIELD)
@Getter @Accessors(fluent = true) @ToString
@NoArgsConstructor(force = true, access = AccessLevel.PACKAGE)
public class Session {

    @EmbeddedId
    private final SessionId id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionType sessionType;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private OffsetDateTime begins;

    @Column(nullable = false)
    private OffsetDateTime ends;

    protected Session(SessionId id, SessionType sessionType, String location, OffsetDateTime begins, OffsetDateTime ends) {
        this.id = id == null ? new SessionId() : id;
        this.sessionType = notNull(sessionType);
        this.location = notBlank(location);

        Validate.isTrue(ends.isAfter(begins), "Session 'ends' datetime must be after 'begins' datetime");
        this.begins = notNull(begins);
        this.ends = notNull(ends);
    }

    protected Session(SessionType sessionType, String location, OffsetDateTime begins, OffsetDateTime ends) {
        this(null, sessionType, location, begins, ends);
    }

    protected void sessionType(SessionType sessionType) {
        this.sessionType = notNull(sessionType);
    }

    protected void location(String location) {
        this.location = notNull(location);
    }

    protected void begins(OffsetDateTime begins) {
        Validate.isTrue(ends.isAfter(begins), "Session 'ends' datetime must be after 'begins' datetime");
        this.begins = begins;
    }

    protected void ends(OffsetDateTime ends) {
        Validate.isTrue(ends.isAfter(begins), "Session 'ends' datetime must be after 'begins' datetime");
        this.ends = ends;
    }
}
