package de.tum.ase.kleo.domain.v2;

import de.tum.ase.kleo.domain.v2.id.PassId;
import de.tum.ase.kleo.domain.v2.id.UserId;
import eu.socialedge.ddd.domain.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.time.Duration;
import java.time.OffsetDateTime;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.Validate.notNull;

@javax.persistence.Entity @Access(AccessType.FIELD)
@NoArgsConstructor(force = true, access = AccessLevel.PACKAGE)
@Getter(AccessLevel.PROTECTED) @Accessors(fluent = true) @ToString
public class Pass extends Entity<PassId> {

    private final static Duration DEFAULT_EXPIRE = Duration.ofMinutes(15);

    @Column(nullable = false)
    private final UserId requesterId;

    @OneToOne
    @JoinColumn(name = "requestee_user_id")
    private final UserId requesteeId;

    @Column(nullable = false)
    private final OffsetDateTime requestedAt = OffsetDateTime.now();

    @Column(nullable = false)
    private final OffsetDateTime expiresAt;

    protected Pass(PassId id, UserId requesterId, UserId requesteeId, Duration expireIn) {
        super(nonNull(id) ? id : new PassId());
        this.requesterId = notNull(requesterId);
        this.requesteeId = notNull(requesteeId);
        this.expiresAt = requestedAt.plus(expireIn);
    }

    protected Pass(UserId requesterId, UserId requesteeId, Duration expireIn) {
        this(null, requesterId, requesteeId, expireIn);
    }

    protected Pass(UserId requesterId, UserId requesteeId) {
        this(null, requesterId, requesteeId, DEFAULT_EXPIRE);
    }

    protected boolean isExpired() {
        return OffsetDateTime.now().isAfter(expiresAt);
    }
}
