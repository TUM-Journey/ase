package de.tum.ase.kleo.application.service;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import de.tum.ase.kleo.domain.Attendance;
import de.tum.ase.kleo.domain.Group;
import de.tum.ase.kleo.domain.GroupRepository;
import de.tum.ase.kleo.domain.Pass;
import de.tum.ase.kleo.domain.PassDetokenizer;
import de.tum.ase.kleo.domain.PassTokenizer;
import de.tum.ase.kleo.domain.Session;
import de.tum.ase.kleo.domain.SessionType;
import de.tum.ase.kleo.domain.User;
import de.tum.ase.kleo.domain.id.SessionId;
import de.tum.ase.kleo.domain.id.UserId;
import de.tum.ase.kleo.ethereum.AttendanceTracker;
import lombok.val;

import static java.util.stream.StreamSupport.stream;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Service
@Transactional(readOnly = true)
public class GroupService {

    private final Logger logger = LoggerFactory.getLogger(GroupService.class);

    private final GroupRepository groupRepository;
    private final UserService userService;

    private final PassTokenizer passTokenizer;
    private final PassDetokenizer passDetokenizer;

    private final AttendanceTracker attendanceTracker;

    public GroupService(GroupRepository groupRepository, UserService userService,
                        PassTokenizer passTokenizer, PassDetokenizer passDetokenizer,
                        AttendanceTracker attendanceTracker) {
        this.groupRepository = groupRepository;
        this.userService = userService;
        this.passTokenizer = passTokenizer;
        this.passDetokenizer = passDetokenizer;
        this.attendanceTracker = attendanceTracker;
    }

    public boolean groupExists(String groupIdOrCode) {
        return groupRepository.existsByIdOrCode(groupIdOrCode);
    }

    @Transactional
    public Group saveGroup(Group group) {
        return groupRepository.save(group);
    }

    public Optional<Group> getGroup(String groupIdOrCode) {
        return groupRepository.findOneByIdOrCode(groupIdOrCode);
    }

    public Stream<Group> getGroups() {
        val groups = groupRepository.findAll();
        return stream(groups.spliterator(), false);
    }

    public Optional<Stream<User>> getGroupStudents(String groupIdOrCode) {
        return getGroup(groupIdOrCode).map(group -> {
            val users = userService.getUsers(group.studentIds());
            return stream(users.spliterator(), false);
        });
    }

    @Transactional
    public Group renameGroup(String groupIdOrCode, String newName) {
        val group = getGroup(groupIdOrCode).orElseThrow(()
                -> new RecordNotFoundException("Unknown group id or code", Group.class));

        if (!isBlank(newName)) {
            group.rename(newName);
        }

        return group;
    }

    @Transactional
    public Group updateGroup(String groupIdOrCode, String newName, Set<UserId> newStudents) {
        val group = getGroup(groupIdOrCode).orElseThrow(()
                -> new RecordNotFoundException("Unknown group id or code", Group.class));

        if (!isBlank(newName)) {
            group.rename(newName);
        }

        if (newStudents != null) {
            group.studentIds(newStudents);
        }

        return group;
    }

    @Transactional
    public Session updateGroupSession(String groupIdOrCode, SessionId sessionId,
                                      SessionType newSessionType, String newLocation,
                                      OffsetDateTime newBegins, OffsetDateTime newEnds) {
        val group = getGroup(groupIdOrCode).orElseThrow(()
                -> new RecordNotFoundException("Unknown group id or code", Group.class));

        if (!group.session(sessionId).isPresent())
            throw new RecordNotFoundException("Unknown session id for group given", Session.class);

        if (newSessionType != null) {
            group.repurposeSession(sessionId, newSessionType);
        }
        if (!isBlank(newLocation)) {
            group.relocateSession(sessionId, newLocation);
        }
        if (newBegins != null && newEnds != null) {
            group.rescheduleSession(sessionId, newBegins, newEnds);
        }

        return group.session(sessionId).get();
    }

    public String generateSessionPassCode(String groupIdOrCode, SessionId sessionId, UserId studentId) {
        val group = getGroup(groupIdOrCode).orElseThrow(()
                -> new RecordNotFoundException("Unknown group id or code", Group.class));

        group.session(sessionId).orElseThrow(()
                -> new RecordNotFoundException("Unknown session id", Session.class));

        final Pass newPass = new Pass(sessionId, studentId);
        return passTokenizer.tokenizeToString(newPass);
    }

    @Transactional
    public void utilizeSessionPassCode(String groupIdOrCode, String passCode) {
        val group = getGroup(groupIdOrCode).orElseThrow(()
                -> new RecordNotFoundException("Unknown group id or code", Group.class));

        final Pass pass = passDetokenizer.detokenize(passCode);
        val attendanceRecord = group.attend(pass);

        val futureTxReceipt = attendanceTracker.recordAttendance(
                attendanceRecord.sessionId().toString(),
                attendanceRecord.studentId().toString()).sendAsync();

        futureTxReceipt.thenAccept((txReceipt) -> {
            logger.info("Attendance has been recorded to the blockchain." +
                    "TxHash = {}", txReceipt.getTransactionHash());
        });
    }

    @Transactional
    public boolean addGroupStudent(String groupIdOrCode, UserId userId) {
        if (!userService.userExists(userId))
            throw new RecordNotFoundException("Failed to find user with given id", User.class);

        val group = getGroup(groupIdOrCode).orElseThrow(()
                -> new RecordNotFoundException("Unknown group id or code", Group.class));

        return group.addStudent(userId);
    }

    @Transactional
    public Session addGroupSession(String groupIdOrCode, SessionType sessionType,
                                   String location, OffsetDateTime begins, OffsetDateTime ends) {
        val group = getGroup(groupIdOrCode).orElseThrow(()
                -> new RecordNotFoundException("Unknown group id or code", Group.class));

        val newSessionId = group.addSession(sessionType, location, begins, ends);
        return group.session(newSessionId).get();
    }

    @Transactional
    public boolean deleteGroup(String groupIdOrCode) {
        return groupRepository.deleteByIdOrCode(groupIdOrCode);
    }

    @Transactional
    public boolean deleteGroupStudent(String groupIdOrCode, UserId userId) {
        if (!userService.userExists(userId))
            throw new RecordNotFoundException("Failed to find user with given id", User.class);

        val group = getGroup(groupIdOrCode).orElseThrow(()
                -> new RecordNotFoundException("Unknown group id or code", Group.class));

        return group.removeStudent(userId);
    }

    @Transactional
    public boolean deleteGroupSession(String groupIdOrCode, SessionId sessionId) {
        val group = getGroup(groupIdOrCode).orElseThrow(()
                -> new RecordNotFoundException("Unknown group id or code", Group.class));

        return group.removeSession(sessionId);
    }

    public Stream<Pair<Group, Set<Attendance>>> getUserGroupAttendances(UserId userId) {
        return groupRepository.findAllByAttendancesStudentId(userId).stream()
                .map(group -> Pair.of(group, group.attendances(userId)));
    }

    public Stream<Group> getUserGroups(UserId userId) {
        return groupRepository.findAllByStudentIdsContaining(userId).stream();
    }
}
