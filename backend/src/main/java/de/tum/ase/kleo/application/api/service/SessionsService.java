package de.tum.ase.kleo.application.api.service;

import de.tum.ase.kleo.application.api.SessionsApiDelegate;
import de.tum.ase.kleo.application.api.dto.PassDTO;
import de.tum.ase.kleo.application.api.dto.SessionDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class SessionsService implements SessionsApiDelegate {
    @Override
    public ResponseEntity<SessionDTO> addSession(SessionDTO session) {
        return null;
    }

    @Override
    public ResponseEntity<Void> deleteSession(String sessionId) {
        return null;
    }

    @Override
    public ResponseEntity<PassDTO> generateSessionPass(String sessionId, PassDTO pass) {
        return null;
    }

    @Override
    public ResponseEntity<List<SessionDTO>> getSessions(String studentIds, String tutorsIds) {
        return null;
    }

    @Override
    public ResponseEntity<SessionDTO> updateSession(String sessionId, SessionDTO group) {
        return null;
    }

    @Override
    public ResponseEntity<Void> utilizeSessionPass(String sessionId, String passId) {
        return null;
    }
}
