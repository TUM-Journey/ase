package de.tum.ase.kleo.domain;

import de.tum.ase.kleo.domain.id.PassId;
import de.tum.ase.kleo.domain.id.UserId;
import eu.socialedge.ddd.domain.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import java.time.Duration;
import java.time.OffsetDateTime;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.Validate.notNull;

@Getter @Accessors(fluent = true) @ToString
@javax.persistence.Entity @Access(AccessType.FIELD)
@NoArgsConstructor(force = true, access = AccessLevel.PACKAGE)
public class Pass extends Entity<PassId> {

    private final static Duration DEFAULT_EXPIRE = Duration.ofMinutes(15);

    @Column(nullable = false)
    @AttributeOverride(name = "value", column = @Column(name = "requester_user_id"))
    private final UserId requesterId;

    @Column(nullable = false)
    @AttributeOverride(name = "value", column = @Column(name = "requestee_user_id"))
    private final UserId requesteeId;

    @Column(name = "requested_at", nullable = false)
    private final OffsetDateTime requestedAt = OffsetDateTime.now();

    @Column(name = "expires_at", nullable = false)
    private final OffsetDateTime expiresAt;

    protected Pass(PassId passId, UserId requesterId, UserId requesteeId, Duration expireIn) {
        super(nonNull(passId) ? passId : new PassId());
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

    public boolean isExpired() {
        return OffsetDateTime.now().isAfter(expiresAt);
    }
}
