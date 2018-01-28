package de.tum.ase.kleo.application.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import de.tum.ase.kleo.application.api.dto.GroupDTO;
import de.tum.ase.kleo.application.api.dto.GroupFromDtoFactory;
import de.tum.ase.kleo.application.api.dto.GroupToDtoSerializer;
import de.tum.ase.kleo.application.api.dto.PassDTO;
import de.tum.ase.kleo.application.api.dto.SessionDTO;
import de.tum.ase.kleo.application.api.dto.SessionToDtoSerializer;
import de.tum.ase.kleo.application.api.dto.UserDTO;
import de.tum.ase.kleo.application.api.dto.UserToDtoSerializer;
import de.tum.ase.kleo.application.service.GroupService;
import de.tum.ase.kleo.domain.Session;
import de.tum.ase.kleo.domain.SessionType;
import de.tum.ase.kleo.domain.id.SessionId;
import de.tum.ase.kleo.domain.id.UserId;
import lombok.val;

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

@Controller
public class GroupResource implements GroupsApiDelegate {

    private final GroupService groupService;
    private final GroupToDtoSerializer groupSerializer;
    private final GroupFromDtoFactory groupFactory;
    private final UserToDtoSerializer userSerializer;
    private final SessionToDtoSerializer sessionSerializer;

    public GroupResource(GroupService groupService,
                         GroupToDtoSerializer groupSerializer,
                         GroupFromDtoFactory groupFactory,
                         UserToDtoSerializer userSerializer,
                         SessionToDtoSerializer sessionSerializer) {
        this.groupService = groupService;
        this.groupSerializer = groupSerializer;
        this.groupFactory = groupFactory;
        this.userSerializer = userSerializer;
        this.sessionSerializer = sessionSerializer;
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<GroupDTO> addGroup(GroupDTO groupDto) {
        val group = groupFactory.create(groupDto);
        val savedGroup = groupService.saveGroup(group);

        return ResponseEntity.ok(groupSerializer.toDto(savedGroup));
    }

    @Override
    @Transactional
    @PreAuthorize("@currentUser.hasUserId(#userId)")
    public ResponseEntity<Void> addGroupStudent(String groupIdOrCode, String userId) {
        groupService.addGroupStudent(groupIdOrCode, UserId.of(userId));
        return ResponseEntity.ok().build();
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<Void> deleteGroup(String groupIdOrCode) {
        val wasDeleted = groupService.deleteGroup(groupIdOrCode);

        if (wasDeleted) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @Override
    @Transactional
    @PreAuthorize("@currentUser.hasUserId(#userId)")
    public ResponseEntity<Void> deleteGroupStudent(String groupIdOrCode, String userId) {
        val wasRemoved = groupService.deleteGroupStudent(groupIdOrCode, UserId.of(userId));

        if (wasRemoved) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @Override
    public ResponseEntity<List<UserDTO>> getGroupStudents(String groupIdOrCode) {
        val groupStudentsOpt = groupService.getGroupStudents(groupIdOrCode);

        return groupStudentsOpt
                .map(users -> ResponseEntity.ok(users.map(userSerializer::toDto)
                        .collect(toList())))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @Override
    public ResponseEntity<List<GroupDTO>> getGroups() {
        return ResponseEntity.ok(groupService.getGroups()
                .map(groupSerializer::toDto).collect(toList()));
    }

    @Override
    public ResponseEntity<GroupDTO> getGroup(String groupIdOrCode) {
        return groupService.getGroup(groupIdOrCode)
                .map(group -> ResponseEntity.ok(groupSerializer.toDto(group)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<GroupDTO> updateGroup(String groupIdOrCode, GroupDTO groupDto) {
        val newName = groupDto.getName();
        val newUserIds = !isNull(groupDto.getStudentIds())
                ? groupDto.getStudentIds().stream().map(UserId::of).collect(toSet())
                    : (Set<UserId>) null;

        val updatedGroup = groupService.updateGroup(groupIdOrCode, newName, newUserIds);
        return ResponseEntity.ok(groupSerializer.toDto(updatedGroup));
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<PassDTO> generateSessionPass(String groupIdOrCode, PassDTO passDto) {
        val sessionId = SessionId.of(passDto.getSessionId());
        val userId = UserId.of(passDto.getStudentId());

        val passCode = groupService.generateSessionPassCode(groupIdOrCode, sessionId, userId);

        passDto.setCode(passCode);
        return ResponseEntity.ok(passDto);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Void> utilizeSessionPass(String groupIdOrCode, String encodedPass) {
        groupService.utilizeSessionPassCode(groupIdOrCode, encodedPass);
        return ResponseEntity.ok().build();
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<SessionDTO> addGroupSession(String groupIdOrCode, SessionDTO sessionDTO) {
        val newSessionType = SessionType.from(sessionDTO.getType());
        val newLocation = sessionDTO.getLocation();
        val newBegins = sessionDTO.getBegins();
        val newEnds = sessionDTO.getEnds();

        final Session createdSession = groupService.addGroupSession(groupIdOrCode,
                newSessionType, newLocation, newBegins, newEnds);

        return ResponseEntity.ok(sessionSerializer.toDto(createdSession));
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<Void> deleteGroupSession(String groupIdOrCode, String sessionId) {
        groupService.deleteGroupSession(groupIdOrCode, SessionId.of(sessionId));
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<SessionDTO> updateGroupSession(String groupIdOrCode, String sessionIdRaw,
                                                         SessionDTO sessionDTO) {
        val sessionId = SessionId.of(sessionIdRaw);
        val newSessionType = SessionType.from(sessionDTO.getType());
        val newLocation = sessionDTO.getLocation();
        val newBegins = sessionDTO.getBegins();
        val newEnds = sessionDTO.getEnds();

        val updatedSession = groupService.updateGroupSession(groupIdOrCode, sessionId,
                newSessionType, newLocation, newBegins, newEnds);

        return ResponseEntity.ok(sessionSerializer.toDto(updatedSession));
    }
}
