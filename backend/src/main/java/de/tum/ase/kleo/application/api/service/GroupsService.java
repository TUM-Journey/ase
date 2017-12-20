package de.tum.ase.kleo.application.api.service;

import de.tum.ase.kleo.application.api.GroupsApiDelegate;
import de.tum.ase.kleo.application.api.dto.*;
import de.tum.ase.kleo.application.service.PassTokenizationService;
import de.tum.ase.kleo.domain.GroupRepository;
import de.tum.ase.kleo.domain.SessionType;
import de.tum.ase.kleo.domain.UserRepository;
import de.tum.ase.kleo.domain.id.GroupId;
import de.tum.ase.kleo.domain.id.SessionId;
import de.tum.ase.kleo.domain.id.UserId;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Service
@Transactional(readOnly = true)
public class GroupsService implements GroupsApiDelegate {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    private final GroupToDtoSerializer groupToDtoSerializer;
    private final GroupFromDtoFactory groupFromDtoFactory;

    private final UserToDtoSerializer userToDtoSerializer;
    private final PassDtoMapper passDtoMapper;
    private final PassTokenizationService passTokenizationService;

    @Autowired
    public GroupsService(GroupRepository groupRepository, UserRepository userRepository,
                         UserToDtoSerializer userToDtoSerializer, GroupToDtoSerializer groupToDtoSerializer,
                         GroupFromDtoFactory groupFromDtoFactory, PassDtoMapper passDtoMapper, PassTokenizationService passTokenizationService) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.userToDtoSerializer = userToDtoSerializer;
        this.groupToDtoSerializer = groupToDtoSerializer;
        this.groupFromDtoFactory = groupFromDtoFactory;
        this.passDtoMapper = passDtoMapper;
        this.passTokenizationService = passTokenizationService;
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
    public ResponseEntity<Void> addGroupStudent(String groupIdRaw, String userIdRaw) {
        val groupId = GroupId.of(groupIdRaw);
        val studentId = UserId.of(userIdRaw);

        val group = groupRepository.findOne(groupId);
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
    public ResponseEntity<Void> deleteGroup(String groupIdRaw) {
        val groupId = GroupId.of(groupIdRaw);

        if (!groupRepository.exists(groupId))
            return ResponseEntity.notFound().build();

        groupRepository.delete(groupId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Override
    @Transactional
    @PreAuthorize("@currentUser.hasUserId(#userIdRaw)")
    public ResponseEntity<Void> deleteGroupStudent(String groupIdRaw, String userIdRaw) {
        val groupId = GroupId.of(groupIdRaw);
        val studentId = UserId.of(userIdRaw);

        val group = groupRepository.findOne(groupId);
        if (group == null)
            return ResponseEntity.notFound().build();

        // TODO: Add notFound message to distinct 404 resps (relies on issue #2)
        if (!group.removeStudent(studentId))
            return ResponseEntity.notFound().build();

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Override
    public ResponseEntity<List<UserDTO>> getGroupStudents(String groupIdRaw) {
        val groupId = GroupId.of(groupIdRaw);

        val group = groupRepository.findOne(groupId);
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
    @Transactional
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<GroupDTO> updateGroup(String groupIdRaw, GroupDTO groupDto) {
        val groupId = GroupId.of(groupIdRaw);

        val group = groupRepository.findOne(groupId);
        if (group == null)
            return ResponseEntity.notFound().build();

        if (isNotBlank(groupDto.getName()))
            group.rename(groupDto.getName());

        if (groupDto.getStudentIds() != null && !groupDto.getStudentIds().isEmpty())
            group.studentIds(groupDto.getStudentIds().stream().map(UserId::of).collect(toSet()));

        return ResponseEntity.ok(groupToDtoSerializer.toDto(group));
    }

    @Override
    @Transactional
    @PreAuthorize("@currentUser.hasUserId(#userIdRaw)")
    public ResponseEntity<PassDTO> generateSessionPass(String groupIdRaw, PassDTO passDto) {
        val groupId = GroupId.of(groupIdRaw);

        val group = groupRepository.findOne(groupId);
        if (group == null)
            return ResponseEntity.notFound().build();

        val pass = passDtoMapper.fromDto(passDto);

        return ResponseEntity.ok(passDtoMapper.toDto(pass));
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<Void> utilizeSessionPass(String groupIdRaw, String encodedPass) {
        val groupId = GroupId.of(groupIdRaw);
        val pass = passTokenizationService.untokenize(encodedPass);

        val group = groupRepository.findOne(groupId);
        if (group == null)
            return ResponseEntity.notFound().build();

        group.attend(pass);

        return ResponseEntity.ok().build();
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<SessionDTO> addGroupSession(String groupIdRaw, SessionDTO sessDto) {
        val groupId = GroupId.of(groupIdRaw);

        val group = groupRepository.findOne(groupId);
        if (group == null)
            return ResponseEntity.notFound().build();

        group.addSession(SessionType.valueOf(sessDto.getType().toString()),
                sessDto.getLocation(), sessDto.getBegins(), sessDto.getEnds());

        return ResponseEntity.ok().build();
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<Void> deleteGroupSession(String groupIdRaw, String sessionIdRaw) {
        val groupId = GroupId.of(groupIdRaw);
        val sessionId = SessionId.of(sessionIdRaw);

        val group = groupRepository.findOne(groupId);
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
    public ResponseEntity<Void> rescheduleGroupSession(String groupIdRaw, String sessionIdRaw, SessionDTO sessDto) {
        val groupId = GroupId.of(groupIdRaw);
        val sessionId = SessionId.of(sessionIdRaw);

        val group = groupRepository.findOne(groupId);
        if (group == null)
            return ResponseEntity.notFound().build();

        group.rescheduleSession(sessionId, sessDto.getBegins(), sessDto.getEnds());

        return ResponseEntity.ok().build();
    }
}
