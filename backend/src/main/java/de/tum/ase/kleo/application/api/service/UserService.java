package de.tum.ase.kleo.application.api.service;

import de.tum.ase.kleo.application.api.UsersApiDelegate;
import de.tum.ase.kleo.application.api.dto.AttendanceDTO;
import de.tum.ase.kleo.application.api.dto.PassDTO;
import de.tum.ase.kleo.application.api.dto.SessionDTO;
import de.tum.ase.kleo.application.api.dto.UserDTO;
import de.tum.ase.kleo.application.api.mapping.PassToDtoMapper;
import de.tum.ase.kleo.application.api.mapping.UserAttendanceDtoMapper;
import de.tum.ase.kleo.application.api.mapping.UserToDtoMapper;
import de.tum.ase.kleo.domain.UserRepository;
import de.tum.ase.kleo.domain.UserRole;
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
public class UserService implements UsersApiDelegate {

    private final UserRepository userRepository;
    private final UserToDtoMapper userMapper;
    private final PassToDtoMapper passMapper;
    private final UserAttendanceDtoMapper userAttendanceMapper;

    @Autowired
    public UserService(UserRepository userRepository, UserToDtoMapper userMapper, PassToDtoMapper passMapper,
                       UserAttendanceDtoMapper userAttendanceMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passMapper = passMapper;
        this.userAttendanceMapper = userAttendanceMapper;
    }

    @Override
    public ResponseEntity<UserDTO> getUser(String userId) {
        val user = userRepository.findOne(userId);
        if (user == null)
            return ResponseEntity.notFound().build();

        return ResponseEntity.ok(userMapper.map(user));
    }

    @Override
    public ResponseEntity<List<AttendanceDTO>> getUserAttendances(String userId, String courseId) {
        val user = userRepository.findOne(userId);
        if (user == null)
            return ResponseEntity.notFound().build();

        val usedPasses = user.usedPasses();
        val attendances = userAttendanceMapper.map(usedPasses);

        return ResponseEntity.ok(attendances);
    }

    @Override
    public ResponseEntity<List<PassDTO>> getUserPasses(String userId) {
        val user = userRepository.findOne(userId);
        if (user == null)
            return ResponseEntity.notFound().build();

        return ResponseEntity.ok(passMapper.map(user.usedPasses()));
    }

    @Override
    public ResponseEntity<List<UserDTO>> getUsers() {
        val users = userRepository.findAll();
        return ResponseEntity.ok(userMapper.map(users));
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('SUPERUSER')")
    public ResponseEntity<Void> deleteUser(String userId) {
        val user = userRepository.findOne(userId);
        if (user == null)
            return ResponseEntity.notFound().build();

        userRepository.delete(userId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('SUPERUSER')")
    public ResponseEntity<SessionDTO> updateUserRoles(String userId, List<String> roles) {
        val user = userRepository.findOne(userId);
        if (user == null)
            return ResponseEntity.notFound().build();


        val newUserRoles = UserRole.from(roles);
        user.truncateUserRoles();
        newUserRoles.forEach(user::addUserRole);

        return ResponseEntity.ok().build();
    }
}
