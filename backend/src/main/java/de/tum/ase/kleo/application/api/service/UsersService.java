package de.tum.ase.kleo.application.api.service;

import de.tum.ase.kleo.application.api.UsersApiDelegate;
import de.tum.ase.kleo.application.api.dto.*;
import de.tum.ase.kleo.domain.*;
import de.tum.ase.kleo.domain.id.UserId;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final CourseRepository courseRepository;
    private final GroupRepository groupRepository;
    private final SessionRepository sessionRepository;

    private final UserDtoSerializer userDtoSerializer;

    private final AttendanceDtoFactory attendanceDtoFactory;
    private final RegistrationDtoFactory registrationDtoFactory;
    private final TutoringDtoFactory tutoringDtoFactory;

    @Autowired
    public UsersService(UserRepository userRepository, CourseRepository courseRepository,
                        GroupRepository groupRepository, SessionRepository sessionRepository,
                        UserDtoSerializer userDtoSerializer, AttendanceDtoFactory attendanceDtoFactory,
                        RegistrationDtoFactory registrationDtoFactory, TutoringDtoFactory tutoringDtoFactory) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.groupRepository = groupRepository;
        this.sessionRepository = sessionRepository;
        this.userDtoSerializer = userDtoSerializer;
        this.attendanceDtoFactory = attendanceDtoFactory;
        this.registrationDtoFactory = registrationDtoFactory;
        this.tutoringDtoFactory = tutoringDtoFactory;
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
    public ResponseEntity<UserDTO> getUser(String userIdRaw) {
        val userId = UserId.of(userIdRaw);

        val user = userRepository.findOne(userId);
        if (user == null)
            return ResponseEntity.notFound().build();

        return ResponseEntity.ok(userDtoSerializer.toDto(user));
    }

    @Override
    public ResponseEntity<List<AttendanceDTO>> getUserAttendances(String userIdRaw) {
        val userId = UserId.of(userIdRaw);

        val attendanceDtos = new LinkedList<AttendanceDTO>();
        sessionRepository.findAllByAttendancesUserId(userId).forEach(session -> {
            val attendance = session.attendance(userId).orElseThrow(() ->
                    new IllegalStateException("User has to have the attendance since the session asked " +
                            "has been taken directly by repository"));

           groupRepository.findAllBySessionIdsContaining(session.id()).forEach(group -> {
               courseRepository.findAllByGroupIdsContaining(group.id()).forEach(course -> {
                   attendanceDtos.add(attendanceDtoFactory.create(course, group, session, attendance.attendedAt()));
               });
           });
        });

        return ResponseEntity.ok(attendanceDtos);
    }

    @Override
    public ResponseEntity<List<RegistrationDTO>> getUserRegistrations(String userIdRaw) {
        val userId = UserId.of(userIdRaw);

        val registrationDtos = new LinkedList<RegistrationDTO>();
        groupRepository.findAllByStudentIdsContaining(userId).forEach(group -> {
            courseRepository.findAllByGroupIdsContaining(group.id()).forEach(course -> {
                registrationDtos.add(registrationDtoFactory.create(course, group));
            });
        });

        return ResponseEntity.ok(registrationDtos);
    }

    @Override
    public ResponseEntity<List<TutoringDTO>> getUserTutorings(String userIdRaw) {
        val userId = UserId.of(userIdRaw);

        val tutoringsDtos = new LinkedList<TutoringDTO>();
        groupRepository.findAllByTutorIdsContaining(userId).forEach(group -> {
            courseRepository.findAllByGroupIdsContaining(group.id()).forEach(course -> {
                tutoringsDtos.add(tutoringDtoFactory.create(course, group));
            });
        });

        return ResponseEntity.ok(tutoringsDtos);
    }

    @Override
    public ResponseEntity<List<UserDTO>> getUsers() {
        val users = userRepository.findAll();
        return ResponseEntity.ok(userDtoSerializer.toDto(users));
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('SUPERUSER')")
    public ResponseEntity<SessionDTO> updateUserRoles(String userIdRaw, List<String> roles) {
        val userId = UserId.of(userIdRaw);

        val user = userRepository.findOne(userId);
        if (user == null)
            return ResponseEntity.notFound().build();


        val newUserRoles = UserRole.from(roles);
        user.truncateUserRoles();
        newUserRoles.forEach(user::addUserRole);

        return ResponseEntity.ok().build();
    }
}
