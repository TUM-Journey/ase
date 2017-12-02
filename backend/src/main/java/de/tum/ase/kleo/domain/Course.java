package de.tum.ase.kleo.domain;

import lombok.*;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Optional;
import java.util.SortedSet;
import java.util.UUID;

import static java.util.Collections.emptySortedSet;
import static java.util.Collections.unmodifiableSortedSet;
import static org.apache.commons.lang3.Validate.notBlank;
import static org.apache.commons.lang3.Validate.notNull;

@Entity
@Accessors(fluent = true)
@ToString @EqualsAndHashCode(of = "id")
@NoArgsConstructor(force = true, access = AccessLevel.PACKAGE)
public class Course {

    @Id
    @Getter
    @Column(name = "course_id")
    private final String id;

    @Getter
    @Column(nullable = false)
    private String name;

    @Column
    @Getter @Setter
    private String description;

    @OneToMany
    @OrderBy("sort")
    @OrderColumn(name = "session_order")
    @JoinColumn(name="course_id", referencedColumnName="course_id")
    private SortedSet<Session> sessions;

    public Course(String id, String name, String description, LocalDate begins, LocalDate ends, SortedSet<Session> sessions) {
        this.id = notBlank(id);
        this.name = notBlank(name);
        this.description = description;
        this.sessions = notNull(sessions);
    }

    public Course(String name, String description, LocalDate begins, LocalDate ends, SortedSet<Session> sessions) {
        this(UUID.randomUUID().toString(), name, description, begins, ends, sessions);
    }

    public Course(String name, LocalDate begins, LocalDate ends) {
        this(name, null, begins, ends, emptySortedSet());
    }

    public void name(String name) {
        this.name = notBlank(name);
    }

    public SortedSet<Session> sessions() {
        return unmodifiableSortedSet(sessions);
    }

    public void addSession(Session session) {
        sessions.add(notNull(session));
    }

    public void removeSession(Session session) {
        sessions.remove(session);
    }

    public void removeSession(String sessionId) {
        sessions.removeIf(session -> session.id().equals(sessionId));
    }

    public Optional<LocalDate> begins() {
        if (sessions.isEmpty())
            return Optional.empty();

        return Optional.of(sessions.first().begins().toLocalDate());
    }

    public Optional<LocalDate> ends() {
        if (sessions.isEmpty())
            return Optional.empty();

        return Optional.of(sessions.last().ends().toLocalDate());
    }
}
