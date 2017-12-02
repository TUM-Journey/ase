package de.tum.ase.kleo.application.api.service;

import de.tum.ase.kleo.application.api.UsersApiDelegate;
import de.tum.ase.kleo.application.api.dto.AttendanceDTO;
import de.tum.ase.kleo.application.api.dto.PassDTO;
import de.tum.ase.kleo.application.api.dto.UserDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService implements UsersApiDelegate {

    @Override
    public ResponseEntity<UserDTO> getUser(String userId) {
        return null;
    }

    @Override
    public ResponseEntity<List<AttendanceDTO>> getUserAttendances(String userId, String courseId) {
        return null;
    }

    @Override
    public ResponseEntity<PassDTO> getUserPasses(String userId) {
        return null;
    }

    @Override
    public ResponseEntity<List<UserDTO>> getUsers() {
        return null;
    }
}
