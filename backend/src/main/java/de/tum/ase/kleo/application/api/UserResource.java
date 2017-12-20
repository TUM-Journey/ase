package de.tum.ase.kleo.application.api;

import de.tum.ase.kleo.application.api.dto.AttendanceDTO;
import de.tum.ase.kleo.application.api.dto.AttendanceToDtoSerializer;
import de.tum.ase.kleo.application.api.dto.GroupDTO;
import de.tum.ase.kleo.application.api.dto.GroupToDtoSerializer;
import de.tum.ase.kleo.application.api.dto.UserDTO;
import de.tum.ase.kleo.application.api.dto.UserToDtoSerializer;
import de.tum.ase.kleo.application.service.GroupService;
import de.tum.ase.kleo.application.service.UserService;
import de.tum.ase.kleo.domain.UserRole;
import de.tum.ase.kleo.domain.id.UserId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.List;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

@Controller
public class UserResource implements UsersApiDelegate {
    
    private final UserService userService;
    private final GroupService groupService;
    
    private final UserToDtoSerializer userSerializer;
    private final GroupToDtoSerializer groupSerializer;
    private final AttendanceToDtoSerializer attendanceSerializer;

    public UserResource(UserService userService, GroupService groupService,
                        UserToDtoSerializer userSerializer, GroupToDtoSerializer groupSerializer, 
                        AttendanceToDtoSerializer attendanceSerializer) {
        this.userService = userService;
        this.groupService = groupService;
        this.userSerializer = userSerializer;
        this.groupSerializer = groupSerializer;
        this.attendanceSerializer = attendanceSerializer;
    }

    @Override
    public ResponseEntity<UserDTO> getUser(String userId) {
        return userService.getUser(UserId.of(userId))
                .map(userSerializer::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<List<UserDTO>> getUsers() {
        return userService.getUsers()
                .map(userSerializer::toDto)
                .collect(collectingAndThen(toList(), ResponseEntity::ok));
    }

    @Override
    @PreAuthorize("hasRole('SUPERUSER')")
    public ResponseEntity<Void> updateUserRoles(String userId, List<String> roles) {
        if (userService.updateUserRoles(UserId.of(userId), UserRole.from(roles))) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    @PreAuthorize("hasRole('SUPERUSER')")
    public ResponseEntity<Void> deleteUser(String userId) {
        if (userService.deleteUser(UserId.of(userId))) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    @PreAuthorize("hasRole('TUTOR') OR @currentUser.hasUserId(#userId)")
    public ResponseEntity<List<AttendanceDTO>> getStudentAttendances(String userId) {
        return groupService.getUserGroupAttendances(UserId.of(userId))
                .flatMap(groupAtt6s -> attendanceSerializer.toDto(groupAtt6s.getKey(), groupAtt6s.getValue()).stream())
                .collect(collectingAndThen(toList(), ResponseEntity::ok));
    }

    @Override
    @PreAuthorize("@currentUser.hasUserId(#userId)")
    public ResponseEntity<List<GroupDTO>> getStudentGroups(String userId) {
        return groupService.getUserGroups(UserId.of(userId))
                .map(groupSerializer::toDto)
                .collect(collectingAndThen(toList(), ResponseEntity::ok));
    }
}
