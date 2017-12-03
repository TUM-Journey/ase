package de.tum.ase.kleo.domain;

import lombok.*;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.UUID;

import static java.util.Collections.emptySortedSet;
import static java.util.Collections.unmodifiableSet;
import static java.util.Collections.unmodifiableSortedSet;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
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

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "course_tutors",
            joinColumns = @JoinColumn(name = "course_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<User> tutors;

    @OneToMany
    @OrderBy("sort")
    @OrderColumn(name = "session_order")
    @JoinColumn(name="course_id", referencedColumnName="course_id")
    private SortedSet<Session> sessions;

    public Course(String id, String name, String description, Set<User> tutors, SortedSet<Session> sessions) {
        this.id = isNotBlank(id) ? id : UUID.randomUUID().toString();
        this.name = notBlank(name);
        this.description = description;
        this.tutors = notNull(tutors);
        this.sessions = notNull(sessions);
    }

    public Course(String id, String name, String description) {
        this(id, name, description, emptySortedSet(), emptySortedSet());
    }

    public Course(String name, String description, Set<User> tutors, SortedSet<Session> sessions) {
        this(null, name, description, tutors, sessions);
    }

    public Course(String name, String description) {
        this(name, description, emptySortedSet(), emptySortedSet());
    }

    public Course(String name) {
        this(name, null, emptySortedSet(), emptySortedSet());
    }

    public void name(String name) {
        this.name = notBlank(name);
    }

    public SortedSet<Session> sessions() {
        return unmodifiableSortedSet(sessions);
    }

    public Optional<Session> session(String sessionId) {
        return sessions.stream().filter(session -> session.id().equals(sessionId)).findAny();
    }

    public void addSession(Session session) {
        sessions.add(notNull(session));
    }

    public boolean removeSession(Session session) {
        return sessions.remove(session);
    }

    public boolean removeSession(String sessionId) {
        return sessions.removeIf(session -> session.id().equals(sessionId));
    }

    public Set<User> tutors() {
        return unmodifiableSet(tutors);
    }

    public void addTutor(User tutor) {
        tutors.add(notNull(tutor));
    }

    public boolean removeTutor(User tutor) {
        return tutors.remove(tutor);
    }

    public boolean removeTutor(String userId) {
        return tutors.removeIf(tutor -> tutor.id().equals(userId));
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
