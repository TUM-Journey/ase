package de.tum.ase.kleo.domain;

import lombok.*;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.Validate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.apache.commons.lang3.Validate.notBlank;
import static org.apache.commons.lang3.Validate.notNull;

@Entity
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
    private LocalDateTime begins;

    @Getter
    @Column(nullable = false)
    private LocalDateTime ends;

    public Session(String id, String location, String note, LocalDateTime begins, LocalDateTime ends) {
        this.id = id;
        this.location = notBlank(location);
        this.note = note;

        Validate.isTrue(ends.isBefore(begins), "Session 'ends' datetime must be after 'begins' datetime");
        this.begins = begins;
        this.ends = ends;
    }

    public Session(String location, String note, LocalDateTime begins, LocalDateTime ends) {
        this(UUID.randomUUID().toString(), location, note, begins, ends);
    }

    public Session(String location, LocalDateTime begins, LocalDateTime ends) {
        this(location, null, begins, ends);
    }

    public void location(String location) {
        this.location = notNull(location);
    }

    public void begins(LocalDateTime begins) {
        Validate.isTrue(ends.isBefore(begins), "Session 'ends' datetime must be after 'begins' datetime");
        this.begins = begins;
    }

    public void ends(LocalDateTime ends) {
        Validate.isTrue(ends.isBefore(begins), "Session 'ends' datetime must be after 'begins' datetime");
        this.ends = ends;
    }

    @Override
    public int compareTo(Session o) {
        return this.begins.compareTo(o.begins);
    }
}
