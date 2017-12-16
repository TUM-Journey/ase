package de.tum.ase.kleo.application.api.service;

import de.tum.ase.kleo.application.api.UsersApiDelegate;
import de.tum.ase.kleo.application.api.dto.*;
import de.tum.ase.kleo.domain.GroupRepository;
import de.tum.ase.kleo.domain.UserRepository;
import de.tum.ase.kleo.domain.UserRole;
import de.tum.ase.kleo.domain.id.UserId;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class UsersService implements UsersApiDelegate {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final UserToDtoSerializer userToDtoSerializer;
    private final AttendanceToDtoSerializer attendanceToDtoSerializer;
    private final GroupToDtoSerializer groupToDtoSerializer;

    public UsersService(UserRepository userRepository, GroupRepository groupRepository,
                        UserToDtoSerializer userToDtoSerializer, AttendanceToDtoSerializer attendanceToDtoSerializer,
                        GroupToDtoSerializer groupToDtoSerializer) {
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.userToDtoSerializer = userToDtoSerializer;
        this.attendanceToDtoSerializer = attendanceToDtoSerializer;
        this.groupToDtoSerializer = groupToDtoSerializer;
    }

    @Override
    public ResponseEntity<UserDTO> getUser(String userIdRaw) {
        val userId = UserId.of(userIdRaw);

        val user = userRepository.findOne(userId);
        if (user == null)
            return ResponseEntity.notFound().build();

        return ResponseEntity.ok(userToDtoSerializer.toDto(user));
    }

    @Override
    public ResponseEntity<List<UserDTO>> getUsers() {
        val users = userRepository.findAll();
        return ResponseEntity.ok(userToDtoSerializer.toDto(users));
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('SUPERUSER')")
    public ResponseEntity<SessionDTO> updateUserRoles(String userIdRaw, List<String> userRolesRaw) {
        val userId = UserId.of(userIdRaw);

        val user = userRepository.findOne(userId);
        if (user == null)
            return ResponseEntity.notFound().build();


        val newUserRoles = UserRole.from(userRolesRaw);
        user.userRoles(newUserRoles);

        return ResponseEntity.ok().build();
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('SUPERUSER')")
    public ResponseEntity<Void> deleteUser(String userIdRaw) {
        val userId = UserId.of(userIdRaw);

        val user = userRepository.findOne(userId);
        if (user == null)
            return ResponseEntity.notFound().build();

        userRepository.delete(userId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Override
    @PreAuthorize("hasRole('TUTOR') OR @currentUser.hasUserId(#userIdRaw)")
    public ResponseEntity<List<AttendanceDTO>> getStudentAttendances(String userIdRaw) {
        val userId = UserId.of(userIdRaw);
        val attendedGroups = groupRepository.findAllByAttendancesStudentId(userId);

        val attendanceDtos = new LinkedList<AttendanceDTO>();
        attendedGroups.forEach(group -> {
            val attendances = group.attendances(userId);
            attendanceDtos.addAll(attendanceToDtoSerializer.toDto(group, attendances));
        });

        return ResponseEntity.ok(attendanceDtos);
    }

    @Override
    @PreAuthorize("@currentUser.hasUserId(#userIdRaw)")
    public ResponseEntity<List<GroupDTO>> getStudentGroups(String userIdRaw) {
        val userId = UserId.of(userIdRaw);
        val groups = groupRepository.findAllByStudentIdsContaining(userId);

        return ResponseEntity.ok(groupToDtoSerializer.toDto(groups));
    }
}
