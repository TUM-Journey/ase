package de.tum.ase.kleo.domain.v2;

import de.tum.ase.kleo.domain.v2.id.UserId;
import lombok.*;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.apache.commons.lang3.Validate.notNull;
import static org.eclipse.jetty.util.StringUtil.isBlank;

@Embeddable @Access(AccessType.FIELD)
@Getter @Accessors(fluent = true) @ToString @EqualsAndHashCode
@NoArgsConstructor(force = true, access = AccessLevel.PACKAGE)
public class Pass {

    private final static Duration DEFAULT_EXPIRE = Duration.ofMinutes(15);

    @Column(nullable = false)
    private final String code;

    @Column(nullable = false)
    private final UserId requesterId;

    @OneToOne
    @JoinColumn(name = "requestee_user_id")
    private final UserId requesteeId;

    @Column(nullable = false)
    private final OffsetDateTime requestedAt = OffsetDateTime.now();

    @Column(nullable = false)
    private final OffsetDateTime expiresAt;

    public Pass(String code, UserId requesterId, UserId requesteeId, Duration expireIn) {
        this.code = isBlank(code) ? UUID.randomUUID().toString() : code;
        this.requesterId = notNull(requesterId);
        this.requesteeId = notNull(requesteeId);
        this.expiresAt = requestedAt.plus(expireIn);
    }

    public Pass(UserId requesterId, UserId requesteeId, Duration expireIn) {
        this(null, requesterId, requesteeId, expireIn);
    }

    public Pass(UserId requesterId, UserId requesteeId) {
        this(null, requesterId, requesteeId, DEFAULT_EXPIRE);
    }

    public boolean isExpired() {
        return OffsetDateTime.now().isAfter(expiresAt);
    }
}
