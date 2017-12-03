package de.tum.ase.kleo.domain;

import lombok.*;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.Validate;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.Validate.notBlank;
import static org.apache.commons.lang3.Validate.notNull;

@Entity
@Access(AccessType.FIELD)
@Accessors(fluent = true)
@ToString @EqualsAndHashCode(of = "id")
@NoArgsConstructor(force = true, access = AccessLevel.PACKAGE)
public class Session implements Comparable<Session> {

    @Id
    @Getter
    @Column(name = "session_id")
    private final String id;

    @Getter
    @Column(nullable = false)
    private String location;

    @Column
    @Getter @Setter
    private String note;

    @Getter
    @Column(nullable = false)
    private OffsetDateTime begins;

    @Getter
    @Column(nullable = false)
    private OffsetDateTime ends;

    public Session(String id, String location, String note, OffsetDateTime begins, OffsetDateTime ends) {
        this.id = isNotBlank(id) ? id : UUID.randomUUID().toString();
        this.location = notBlank(location);
        this.note = note;

        Validate.isTrue(ends.isAfter(begins), "Session 'ends' datetime must be after 'begins' datetime");
        this.begins = begins;
        this.ends = ends;
    }

    public Session(String location, String note, OffsetDateTime begins, OffsetDateTime ends) {
        this(null, location, note, begins, ends);
    }

    public Session(String location, OffsetDateTime begins, OffsetDateTime ends) {
        this(location, null, begins, ends);
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

    @Override
    public int compareTo(Session o) {
        return this.begins.compareTo(o.begins);
    }
}
