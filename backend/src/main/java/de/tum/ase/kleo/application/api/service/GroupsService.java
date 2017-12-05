package de.tum.ase.kleo.application.api.service;

import de.tum.ase.kleo.application.api.GroupsApiDelegate;
import de.tum.ase.kleo.application.api.dto.*;
import de.tum.ase.kleo.domain.GroupRepository;
import de.tum.ase.kleo.domain.SessionRepository;
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

@Service
@Transactional(readOnly = true)
public class GroupsService implements GroupsApiDelegate {

    private final GroupRepository groupRepository;
    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;

    private final GroupDtoMapper groupDtoMapper;
    private final GroupDtoMerger groupDtoMerger;
    private final SessionDtoMapper sessionDtoMapper;
    private final UserDtoSerializer userDtoSerializer;

    @Autowired
    public GroupsService(GroupRepository groupRepository, SessionRepository sessionRepository,
                         UserRepository userRepository, GroupDtoMapper groupDtoMapper,
                         GroupDtoMerger groupDtoMerger, SessionDtoMapper sessionDtoMapper,
                         UserDtoSerializer userDtoSerializer) {
        this.groupRepository = groupRepository;
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
        this.groupDtoMapper = groupDtoMapper;
        this.groupDtoMerger = groupDtoMerger;
        this.sessionDtoMapper = sessionDtoMapper;
        this.userDtoSerializer = userDtoSerializer;
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('SUPERUSER')")
    public ResponseEntity<GroupDTO> addGroup(GroupDTO groupDto) {
        val group = groupDtoMapper.fromDto(groupDto);
        val groupSaved = groupRepository.save(group);

        return ResponseEntity.ok(groupDtoMapper.toDto(groupSaved));
    }

    @Override
    public ResponseEntity<Void> addGroupSession(String groupIdRaw, String sessionIdRaw) {
        val groupId = GroupId.of(groupIdRaw);
        val sessionId = SessionId.of(sessionIdRaw);

        val group = groupRepository.findOne(groupId);
        if (group == null)
            return ResponseEntity.notFound().build();

        // TODO: Add notFound message to distinct 404 resps (relies on issue #2)
        if (!sessionRepository.exists(sessionId))
            return ResponseEntity.notFound().build();

        group.addSession(sessionId);

        return ResponseEntity.ok().build();
    }

    @Override
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
    @PreAuthorize("hasRole('SUPERUSER')")
    public ResponseEntity<Void> addGroupTutor(String groupIdRaw, String userIdRaw) {
        val groupId = GroupId.of(groupIdRaw);
        val tutorId = UserId.of(userIdRaw);

        val group = groupRepository.findOne(groupId);
        if (group == null)
            return ResponseEntity.notFound().build();

        // TODO: Add notFound message to distinct 404 resps (relies on issue #2)
        if (!userRepository.exists(tutorId))
            return ResponseEntity.notFound().build();

        group.addTutor(tutorId);

        return ResponseEntity.ok().build();
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('SUPERUSER')")
    public ResponseEntity<Void> deleteGroup(String groupIdRaw) {
        val groupId = GroupId.of(groupIdRaw);

        if (!groupRepository.exists(groupId))
            return ResponseEntity.notFound().build();

        groupRepository.delete(groupId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Override
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
    @Transactional
    @PreAuthorize("hasRole('SUPERUSER')")
    public ResponseEntity<Void> deleteGroupTutor(String groupIdRaw, String userIdRaw) {
        val groupId = GroupId.of(groupIdRaw);
        val tutorId = UserId.of(userIdRaw);

        val group = groupRepository.findOne(groupId);
        if (group == null)
            return ResponseEntity.notFound().build();

        // TODO: Add notFound message to distinct 404 resps (relies on issue #2)
        if (!group.removeTutor(tutorId))
            return ResponseEntity.notFound().build();

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Override
    public ResponseEntity<List<SessionDTO>> getGroupSessions(String groupIdRaw) {
        val groupId = GroupId.of(groupIdRaw);

        val group = groupRepository.findOne(groupId);
        if (group == null)
            return ResponseEntity.notFound().build();

        val sessions = sessionRepository.findAll(group.sessionIds());
        return ResponseEntity.ok(sessionDtoMapper.toDto(sessions));
    }

    @Override
    public ResponseEntity<List<UserDTO>> getGroupStudents(String groupIdRaw) {
        val groupId = GroupId.of(groupIdRaw);

        val group = groupRepository.findOne(groupId);
        if (group == null)
            return ResponseEntity.notFound().build();

        val students = userRepository.findAll(group.studentIds());
        return ResponseEntity.ok(userDtoSerializer.toDto(students));
    }

    @Override
    public ResponseEntity<List<UserDTO>> getGroupTutors(String groupIdRaw) {
        val groupId = GroupId.of(groupIdRaw);

        val group = groupRepository.findOne(groupId);
        if (group == null)
            return ResponseEntity.notFound().build();

        val tutors = userRepository.findAll(group.tutorIds());
        return ResponseEntity.ok(userDtoSerializer.toDto(tutors));
    }

    @Override
    public ResponseEntity<List<GroupDTO>> getGroups() {
        return ResponseEntity.ok(groupDtoMapper.toDto(groupRepository.findAll()));
    }

    @Override
    public ResponseEntity<GroupDTO> updateGroup(String groupIdRaw, GroupDTO groupDto) {
        val groupId = GroupId.of(groupIdRaw);

        val group = groupRepository.findOne(groupId);
        if (group == null)
            return ResponseEntity.notFound().build();

        groupDtoMerger.merge(groupDto, group);

        return ResponseEntity.ok(groupDtoMapper.toDto(group));
    }
}
