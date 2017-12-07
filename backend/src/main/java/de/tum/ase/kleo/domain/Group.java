package de.tum.ase.kleo.domain;


import de.tum.ase.kleo.domain.id.GroupId;
import de.tum.ase.kleo.domain.id.SessionId;
import de.tum.ase.kleo.domain.id.UserId;
import lombok.*;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.Validate;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.Validate.notBlank;

@Accessors(fluent = true) @ToString
@Entity(name = "GR0UP") @Access(AccessType.FIELD)
@NoArgsConstructor(force = true, access = AccessLevel.PACKAGE)
public class Group {

    @Getter
    @EmbeddedId
    private final GroupId id;

    @Getter
    @Column(nullable = false)
    private String name;

    @ElementCollection
    @CollectionTable(name = "course_students", joinColumns = @JoinColumn(name = "group_id"))
    private final Set<UserId> studentIds = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "group_tutors", joinColumns = @JoinColumn(name = "group_id"))
    private final Set<UserId> tutorIds = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", referencedColumnName = "group_id")
    private final List<Session> sessions = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "session_passes", joinColumns = @JoinColumn(name = "group_id"))
    private final Set<Pass> passes = new HashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "session_attendances", joinColumns = @JoinColumn(name = "group_id"))
    private final Set<Attendance> attendances = new HashSet<>();

    public Group(GroupId id, String name) {
        this.id = id == null ? new GroupId() : id;
        this.name = notBlank(name);
    }

    public Group(String name) {
        this(null, name);
    }

    public void rename(String name) {
        this.name = notBlank(name);
    }

    public boolean addStudent(UserId studentId) {
        return studentIds.add(studentId);
    }

    public boolean hasStudent(UserId studentId) {
        return studentIds.stream().anyMatch(sId -> sId.equals(studentId));
    }

    public Set<UserId> studentIds() {
        return Collections.unmodifiableSet(studentIds);
    }

    public boolean removeStudent(UserId studentId) {
        return studentIds.removeIf(sId -> sId.equals(studentId));
    }

    public boolean addTutor(UserId tutorId) {
        return tutorIds.add(tutorId);
    }

    public boolean hasTutor(UserId tutorId) {
        return tutorIds.stream().anyMatch(tId -> tId.equals(tutorId));
    }

    public Set<UserId> tutorIds() {
        return Collections.unmodifiableSet(tutorIds);
    }

    public boolean removeTutor(UserId tutorId) {
        return tutorIds.removeIf(tId -> tId.equals(tutorId));
    }

    public SessionId addSession(SessionId sessionId, SessionType sessionType, String location, OffsetDateTime begins, OffsetDateTime ends) {
        val newSession = new Session(sessionId, sessionType, location, begins, ends);
        sessions.add(newSession);
        return newSession.id();
    }

    public SessionId addSession(SessionType sessionType, String location, OffsetDateTime begins, OffsetDateTime ends) {
        return addSession(null, sessionType, location, begins, ends);
    }

    public List<Session> sessions() {
        return Collections.unmodifiableList(sessions);
    }

    public List<Session> sessions(SessionType sessionType) {
        return sessions.stream()
                .filter(s -> s.sessionType().equals(sessionType))
                .collect(Collectors.toList());
    }

    public Optional<Session> session(SessionId sessionId) {
        return sessions.stream().filter(s -> s.id().equals(sessionId)).findAny();
    }

    public void repurposeSession(SessionId sessionId, SessionType sessionType) {
        session(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("No session found by sessionId given"))
                .sessionType(sessionType);
    }

    public void relocateSession(SessionId sessionId, String location) {
        session(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("No session found by sessionId given"))
                .location(location);
    }

    public void rescheduleSession(SessionId sessionId, OffsetDateTime begins, OffsetDateTime ends) {
        val session = session(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("No session found by sessionId given"));

        Validate.isTrue(ends.isAfter(begins), "Session 'ends' datetime must be after 'begins' datetime");
        session.begins(begins);
        session.ends(ends);
    }

    public boolean removeSession(SessionId sessionId) {
        return sessions.removeIf(s -> s.id().equals(sessionId));
    }

    public boolean registerPass(Pass pass) {
        if (hasPass(pass.studentId(), pass.sessionId()))
            throw new IllegalArgumentException("Only one Pass per student & session is allowed");

        return passes.add(pass);
    }

    public boolean hasPass(UserId studentId, SessionId sessionId) {
        return passes.stream()
                .anyMatch(pass -> pass.studentId().equals(studentId)
                        && pass.sessionId().equals(sessionId));
    }

    public boolean isPassUsed(UUID passCode) {
        return attendances.stream().anyMatch(a -> a.passCode().equals(passCode));
    }

    public Set<Pass> passes() {
        return Collections.unmodifiableSet(passes);
    }

    protected Optional<Pass> validPass(UUID passCode) {
        return passes.stream()
                .filter(pass -> pass.code().equals(passCode))
                .filter(pass -> !isPassUsed(passCode))
                .filter(Pass::notExpired)
                .findAny();
    }

    public Attendance attend(UUID passCode) {
        val eligiblePass = validPass(passCode)
                .orElseThrow(() -> new IllegalArgumentException("Pass with given code is expired or already used"));

        val newAttendance = new Attendance(passCode, eligiblePass.sessionId(), eligiblePass.tutorId());
        attendances.add(newAttendance);
        return newAttendance;
    }

    public boolean hasAttended(UserId studentId, SessionId sessionId) {
        return attendances.stream().anyMatch(a -> a.studentId().equals(studentId)
                && a.sessionId().equals(sessionId));
    }

    public Set<Attendance> attendances(UserId studentId) {
        return attendances.stream()
                .filter(a -> a.studentId().equals(studentId))
                .collect(Collectors.toSet());
    }

    public Set<Attendance> attendances(SessionId sessionId) {
        return attendances.stream()
                .filter(a -> a.sessionId().equals(sessionId))
                .collect(Collectors.toSet());
    }

    public Set<Attendance> attendances() {
        return Collections.unmodifiableSet(attendances);
    }
}