package de.tum.ase.kleo.application.api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import de.tum.ase.kleo.application.api.GroupsApiDelegate;
import de.tum.ase.kleo.application.api.dto.GroupDTO;
import de.tum.ase.kleo.application.api.dto.GroupFromDtoFactory;
import de.tum.ase.kleo.application.api.dto.GroupToDtoSerializer;
import de.tum.ase.kleo.application.api.dto.PassDTO;
import de.tum.ase.kleo.application.api.dto.PassDtoMapper;
import de.tum.ase.kleo.application.api.dto.SessionDTO;
import de.tum.ase.kleo.application.api.dto.UserDTO;
import de.tum.ase.kleo.application.api.dto.UserToDtoSerializer;
import de.tum.ase.kleo.domain.GroupRepository;
import de.tum.ase.kleo.domain.PassDetokenizer;
import de.tum.ase.kleo.domain.SessionType;
import de.tum.ase.kleo.domain.UserRepository;
import de.tum.ase.kleo.domain.id.SessionId;
import de.tum.ase.kleo.domain.id.UserId;
import de.tum.ase.kleo.ethereum.AttendanceTracker;
import lombok.val;

import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Service
@Transactional(readOnly = true)
@Deprecated // TODO Refactor see UserResource
public class GroupsService implements GroupsApiDelegate {

    private final Logger logger = LoggerFactory.getLogger(GroupsService.class);

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    private final GroupToDtoSerializer groupToDtoSerializer;
    private final GroupFromDtoFactory groupFromDtoFactory;

    private final UserToDtoSerializer userToDtoSerializer;
    private final PassDtoMapper passDtoMapper;
    private final PassDetokenizer passDetokenizer;

    private final AttendanceTracker attendanceTracker;

    @Autowired
    public GroupsService(GroupRepository groupRepository, UserRepository userRepository,
                         UserToDtoSerializer userToDtoSerializer, GroupToDtoSerializer groupToDtoSerializer,
                         GroupFromDtoFactory groupFromDtoFactory, PassDtoMapper passDtoMapper,
                         PassDetokenizer passDetokenizer, AttendanceTracker attendanceTracker) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.userToDtoSerializer = userToDtoSerializer;
        this.groupToDtoSerializer = groupToDtoSerializer;
        this.groupFromDtoFactory = groupFromDtoFactory;
        this.passDtoMapper = passDtoMapper;
        this.passDetokenizer = passDetokenizer;
        this.attendanceTracker = attendanceTracker;
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<GroupDTO> addGroup(GroupDTO groupDto) {
        val group = groupFromDtoFactory.create(groupDto);
        val savedGroup = groupRepository.save(group);

        return ResponseEntity.ok(groupToDtoSerializer.toDto(savedGroup));
    }

    @Override
    @Transactional
    @PreAuthorize("@currentUser.hasUserId(#userIdRaw)")
    public ResponseEntity<Void> addGroupStudent(String groupIdOrCodeRaw, String userIdRaw) {
        val studentId = UserId.of(userIdRaw);

        val group = groupRepository.fetchGroupByIdOrCode(groupIdOrCodeRaw);
        if (group == null)
            return ResponseEntity.notFound().build();

        // TODO: Add notFound message to distinct 404 resps (relies on issue #2)
        if (!userRepository.exists(studentId))
            return ResponseEntity.notFound().build();

        group.addStudent(studentId);

        return ResponseEntity.ok().build();
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<Void> deleteGroup(String groupIdOrCodeRaw) {
        val group = groupRepository.fetchGroupByIdOrCode(groupIdOrCodeRaw);
        if (group == null)
            return ResponseEntity.notFound().build();

        groupRepository.delete(group);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Override
    @Transactional
    @PreAuthorize("@currentUser.hasUserId(#userIdRaw)")
    public ResponseEntity<Void> deleteGroupStudent(String groupIdOrCodeRaw, String userIdRaw) {
        val studentId = UserId.of(userIdRaw);

        val group = groupRepository.fetchGroupByIdOrCode(groupIdOrCodeRaw);
        if (group == null)
            return ResponseEntity.notFound().build();

        // TODO: Add notFound message to distinct 404 resps (relies on issue #2)
        if (!group.removeStudent(studentId))
            return ResponseEntity.notFound().build();

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Override
    public ResponseEntity<List<UserDTO>> getGroupStudents(String groupIdOrCodeRaw) {
        val group = groupRepository.fetchGroupByIdOrCode(groupIdOrCodeRaw);
        if (group == null)
            return ResponseEntity.notFound().build();

        val students = userRepository.findAll(group.studentIds());
        return ResponseEntity.ok(userToDtoSerializer.toDto(students));
    }

    @Override
    public ResponseEntity<List<GroupDTO>> getGroups() {
        return ResponseEntity.ok(groupToDtoSerializer.toDto(groupRepository.findAll()));
    }

    @Override
    public ResponseEntity<GroupDTO> getGroup(String groupIdOrCodeRaw) {
        val group = groupRepository.fetchGroupByIdOrCode(groupIdOrCodeRaw);
        if (group == null)
            return ResponseEntity.notFound().build();

        return ResponseEntity.ok(groupToDtoSerializer.toDto(group));
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<GroupDTO> updateGroup(String groupIdOrCodeRaw, GroupDTO groupDto) {
        val group = groupRepository.fetchGroupByIdOrCode(groupIdOrCodeRaw);
        if (group == null)
            return ResponseEntity.notFound().build();

        if (isNotBlank(groupDto.getName()))
            group.rename(groupDto.getName());

        if (groupDto.getStudentIds() != null && !groupDto.getStudentIds().isEmpty())
            group.studentIds(groupDto.getStudentIds().stream().map(UserId::of).collect(toSet()));

        if (groupDto.getSessions() != null && !groupDto.getSessions().isEmpty()) {
            val changedSessionIdToSession = groupDto.getSessions().stream()
                    .collect(toMap(SessionDTO::getId, Function.identity()));

            group.sessions().stream()
                    .filter(s -> changedSessionIdToSession.containsKey(s.id().toString()))
                    .forEach(sessionToUpdate -> {
                        val updatedSession = changedSessionIdToSession
                                .get(sessionToUpdate.id().toString());

                        val newLocation = updatedSession.getLocation();
                        if (isNotBlank(newLocation)) {
                            group.relocateSession(sessionToUpdate.id(), newLocation);
                        }

                        val newType = updatedSession.getType();
                        if (nonNull(newType)) {
                            group.repurposeSession(sessionToUpdate.id(),
                                    SessionType.valueOf(newType.name()));
                        }
                    });
        }

        return ResponseEntity.ok(groupToDtoSerializer.toDto(group));
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<PassDTO> generateSessionPass(String groupIdOrCodeRaw, PassDTO passDto) {
        val group = groupRepository.fetchGroupByIdOrCode(groupIdOrCodeRaw);
        if (group == null)
            return ResponseEntity.notFound().build();

        val pass = passDtoMapper.fromDto(passDto);

        return ResponseEntity.ok(passDtoMapper.toDto(pass));
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Void> utilizeSessionPass(String groupIdOrCodeRaw, String encodedPass) {
        val pass = passDetokenizer.detokenize(encodedPass);

        val group = groupRepository.fetchGroupByIdOrCode(groupIdOrCodeRaw);
        if (group == null)
            return ResponseEntity.notFound().build();

        val attendanceRecord = group.attend(pass);
        val futureTxReceipt = attendanceTracker.recordAttendance(
                attendanceRecord.sessionId().toString(),
                attendanceRecord.studentId().toString()).sendAsync();

        futureTxReceipt.thenAccept((txReceipt) -> {
            logger.info("Attendance has been recorded to the blockchain." +
                    "TxHash = {}", txReceipt.getTransactionHash());
        });

        return ResponseEntity.ok().build();
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<SessionDTO> addGroupSession(String groupIdOrCodeRaw, SessionDTO sessDto) {
        val group = groupRepository.fetchGroupByIdOrCode(groupIdOrCodeRaw);
        if (group == null)
            return ResponseEntity.notFound().build();

        final SessionId newSessionId =
                group.addSession(SessionType.valueOf(sessDto.getType().toString()),
                    sessDto.getLocation(), sessDto.getBegins(), sessDto.getEnds());

        sessDto.setId(newSessionId.toString());

        return ResponseEntity.ok(sessDto);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<Void> deleteGroupSession(String groupIdOrCodeRaw, String sessionIdRaw) {
        val sessionId = SessionId.of(sessionIdRaw);

        val group = groupRepository.fetchGroupByIdOrCode(groupIdOrCodeRaw);
        if (group == null)
            return ResponseEntity.notFound().build();

        // TODO: Add notFound message to distinct 404 resps (relies on issue #2)
        if (!group.removeSession(sessionId))
            return ResponseEntity.notFound().build();

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<Void> rescheduleGroupSession(String groupIdOrCodeRaw, String sessionIdRaw, SessionDTO sessDto) {
        val sessionId = SessionId.of(sessionIdRaw);

        val group = groupRepository.fetchGroupByIdOrCode(groupIdOrCodeRaw);
        if (group == null)
            return ResponseEntity.notFound().build();

        group.rescheduleSession(sessionId, sessDto.getBegins(), sessDto.getEnds());

        return ResponseEntity.ok().build();
    }
}
