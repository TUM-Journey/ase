package de.tum.ase.kleo.domain;

import de.tum.ase.kleo.domain.id.GroupId;
import de.tum.ase.kleo.domain.id.SessionId;
import de.tum.ase.kleo.domain.id.UserId;
import eu.socialedge.ddd.domain.AggregateRoot;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.Validate.notBlank;
import static org.apache.commons.lang3.Validate.notNull;

@Entity @Access(AccessType.FIELD)
@Accessors(fluent = true) @ToString
@NoArgsConstructor(force = true, access = AccessLevel.PACKAGE)
public class Group extends AggregateRoot<GroupId> {

    @Getter
    @Column(nullable = false)
    private String name;

    @ElementCollection
    @CollectionTable(name = "course_students", joinColumns = @JoinColumn(name = "group_id"))
    private final Set<UserId> studentIds = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "group_tutors", joinColumns = @JoinColumn(name = "group_id"))
    private final Set<UserId> tutorIds = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "group_sessions", joinColumns = @JoinColumn(name = "group_id"))
    private final Set<SessionId> sessionIds = new HashSet<>();

    public Group(GroupId id, String name, Set<UserId> studentIds, Set<UserId> tutorIds, Set<SessionId> sessionIds) {
        super(nonNull(id) ? id : new GroupId());
        this.name = notBlank(name);

        if (tutorIds != null)
            this.tutorIds.addAll(tutorIds);

        if (studentIds != null)
            this.studentIds.addAll(studentIds);

        if (sessionIds != null)
            this.sessionIds.addAll(sessionIds);
    }

    public Group(String name, Set<UserId> studentIds, Set<UserId> tutorIds, Set<SessionId> sessionIds) {
        this(null, name, studentIds, tutorIds, sessionIds);
    }

    public Group(String name) {
        this(name, null, null, null);
    }

    public boolean hasTutor(UserId userId) {
        return tutorIds.stream().anyMatch(id -> id.equals(userId));
    }

    public void addTutor(UserId tutorId) {
        tutorIds.add(notNull(tutorId));
    }

    public boolean removeTutor(UserId tutorId) {
        return tutorIds.remove(tutorId);
    }

    public Set<UserId> tutorIds() {
        return unmodifiableSet(tutorIds);
    }

    public boolean hasStudent(UserId userId) {
        return studentIds.stream().anyMatch(id -> id.equals(userId));
    }

    public void addStudent(UserId studentId) {
        studentIds.add(notNull(studentId));
    }

    public boolean removeStudent(UserId studentId) {
        return tutorIds.remove(studentId);
    }

    public Set<UserId> studentIds() {
        return unmodifiableSet(studentIds);
    }

    public boolean hasSession(SessionId sessionId) {
        return sessionIds.stream().anyMatch(id -> id.equals(sessionId));
    }

    public void addSession(SessionId sessionId) {
        sessionIds.add(notNull(sessionId));
    }

    public boolean removeSession(SessionId sessionId) {
        return sessionIds.remove(sessionId);
    }

    public Set<SessionId> sessionIds() {
        return unmodifiableSet(sessionIds);
    }
}
