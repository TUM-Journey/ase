package de.tum.ase.kleo.application.api.service;

import de.tum.ase.kleo.application.api.UsersApiDelegate;
import de.tum.ase.kleo.application.api.dto.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class UsersService implements UsersApiDelegate {

    @Override
    public ResponseEntity<Void> deleteUser(String userId) {
        return null;
    }

    @Override
    public ResponseEntity<UserDTO> getUser(String userId) {
        return null;
    }

    @Override
    public ResponseEntity<List<AttendanceDTO>> getUserAttendances(String userId) {
        return null;
    }

    @Override
    public ResponseEntity<List<RegistrationDTO>> getUserRegistrations(String userId) {
        return null;
    }

    @Override
    public ResponseEntity<List<TutoringDTO>> getUserTutorings(String userId) {
        return null;
    }

    @Override
    public ResponseEntity<List<UserDTO>> getUsers() {
        return null;
    }

    @Override
    public ResponseEntity<SessionDTO> updateUserRoles(String userId, List<String> roles) {
        return null;
    }
}
