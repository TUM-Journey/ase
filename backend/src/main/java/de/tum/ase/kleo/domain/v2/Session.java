package de.tum.ase.kleo.domain.v2;

import de.tum.ase.kleo.domain.v2.id.PassId;
import de.tum.ase.kleo.domain.v2.id.SessionId;
import de.tum.ase.kleo.domain.v2.id.UserId;
import eu.socialedge.ddd.domain.AggregateRoot;
import lombok.*;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.Validate;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.Validate.notBlank;
import static org.apache.commons.lang3.Validate.notNull;

@Entity @Access(AccessType.FIELD)
@Accessors(fluent = true) @ToString
@NoArgsConstructor(force = true, access = AccessLevel.PACKAGE)
public class Session extends AggregateRoot<SessionId> implements Comparable<Session> {

    @Getter
    @Column(nullable = false)
    private String location;

    @Getter
    @Column(nullable = false)
    private OffsetDateTime begins;

    @Getter
    @Column(nullable = false)
    private OffsetDateTime ends;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "session_passes", joinColumns = @JoinColumn(name = "session_id"))
    private final Set<Pass> passes = new HashSet<>();

    @Getter
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "session_attendances", joinColumns = @JoinColumn(name = "session_id"))
    private final Set<Attendance> attendances = new HashSet<>();

    public Session(SessionId id, String location, OffsetDateTime begins, OffsetDateTime ends) {
        super(nonNull(id) ? id : new SessionId());
        this.location = notBlank(location);

        Validate.isTrue(ends.isAfter(begins), "Session 'ends' datetime must be after 'begins' datetime");
        this.begins = begins;
        this.ends = ends;
    }

    public Session(String location, OffsetDateTime begins, OffsetDateTime ends) {
        this(null, location, begins, ends);
    }

    public void location(String location) {
        this.location = notNull(location);
    }

    public void begins(OffsetDateTime begins) {
        Validate.isTrue(ends.isAfter(begins), "Session 'ends' datetime must be after 'begins' datetime");
        this.begins = begins;
    }

    public void ends(OffsetDateTime ends) {
        Validate.isTrue(ends.isAfter(begins), "Session 'ends' datetime must be after 'begins' datetime");
        this.ends = ends;
    }

    public PassId createPass(UserId requesterId, UserId requesteeId) {
        if (hasNonExpiredPass(requesteeId))
            throw new IllegalStateException("User already has the nonExpiredPass for this session");

        val pass = new Pass(requesterId, requesteeId);
        passes.add(pass);
        return pass.id();
    }

    public boolean hasNonExpiredPass(PassId passId, UserId requesteeId) {
        return nonExpiredPass(passId).filter(pass -> pass.requesteeId().equals(requesteeId)).isPresent();
    }

    public Attendance attend(PassId passId) {
        val pass = nonExpiredPass(passId).orElseThrow(()
                -> new IllegalArgumentException("No valid pass with given id found"));

        val attendance = new Attendance(id, pass.requesteeId());
        attendances.add(attendance);

        return attendance;
    }

    private Optional<Pass> nonExpiredPass(PassId passId) {
        return passes.stream().filter(pass -> pass.id().equals(passId)).findAny()
                .filter(pass -> {
                    if (pass.isExpired()) {
                        passes.remove(pass);
                        return false;
                    }
                    return true;
                });
    }

    private boolean hasNonExpiredPass(UserId requesteeId) {
        return passes.stream().filter(pass -> pass.requesteeId().equals(requesteeId)).findAny()
                .filter(pass -> {
                    if (pass.isExpired()) {
                        passes.remove(pass);
                        return false;
                    }
                    return true;
                }).isPresent();
    }

    @Override
    public int compareTo(Session o) {
        return this.begins.compareTo(o.begins);
    }
}
