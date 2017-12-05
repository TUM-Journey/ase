package de.tum.ase.kleo.application.api.service;

import de.tum.ase.kleo.application.api.SessionsApiDelegate;
import de.tum.ase.kleo.application.api.dto.PassDTO;
import de.tum.ase.kleo.application.api.dto.SessionDTO;
import de.tum.ase.kleo.application.api.dto.SessionDtoMapper;
import de.tum.ase.kleo.application.api.dto.SessionDtoMerger;
import de.tum.ase.kleo.domain.SessionRepository;
import de.tum.ase.kleo.domain.id.PassId;
import de.tum.ase.kleo.domain.id.SessionId;
import de.tum.ase.kleo.domain.id.UserId;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class SessionsService implements SessionsApiDelegate {

    private final SessionRepository sessionRepository;
    private final SessionDtoMapper sessionDtoMapper;
    private final SessionDtoMerger sessionDtoMerger;

    public SessionsService(SessionRepository sessionRepository, SessionDtoMapper sessionDtoMapper, SessionDtoMerger sessionDtoMerger) {
        this.sessionRepository = sessionRepository;
        this.sessionDtoMapper = sessionDtoMapper;
        this.sessionDtoMerger = sessionDtoMerger;
    }

    @Override
    public ResponseEntity<SessionDTO> addSession(SessionDTO dto) {
        val session = sessionDtoMapper.fromDto(dto);
        val savedSession = sessionRepository.save(session);

        return ResponseEntity.ok(sessionDtoMapper.toDto(savedSession));
    }

    @Override
    public ResponseEntity<Void> deleteSession(String sessionIdRaw) {
        val sessionId = SessionId.of(sessionIdRaw);

        if (!sessionRepository.exists(sessionId))
            return ResponseEntity.notFound().build();

        sessionRepository.delete(sessionId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Override
    public ResponseEntity<String> generateSessionPass(String sessionIdRaw, PassDTO passDto) {
        val sessionId = SessionId.of(sessionIdRaw);

        val session = sessionRepository.findOne(sessionId);
        if (session == null)
            return ResponseEntity.notFound().build();

        val passId = session.addPass(UserId.of(passDto.getRequesterId()), UserId.of(passDto.getRequesteeId()));

        return ResponseEntity.ok(passId.toString());
    }

    @Override
    public ResponseEntity<List<SessionDTO>> getSessions() {
        return ResponseEntity.ok(sessionDtoMapper.toDto(sessionRepository.findAll()));
    }

    @Override
    public ResponseEntity<SessionDTO> updateSession(String sessionIdRaw, SessionDTO dto) {
        val sessionId = SessionId.of(sessionIdRaw);

        val session = sessionRepository.findOne(sessionId);
        if (session == null)
            return ResponseEntity.notFound().build();

        sessionDtoMerger.merge(dto, session);

        return ResponseEntity.ok(sessionDtoMapper.toDto(session));
    }

    @Override
    public ResponseEntity<Void> utilizeSessionPass(String sessionIdRaw, String passIdRaw) {
        val sessionId = SessionId.of(sessionIdRaw);
        val passId = PassId.of(passIdRaw);

        val session = sessionRepository.findOne(sessionId);
        if (session == null)
            return ResponseEntity.notFound().build();

        session.attend(passId);

        return ResponseEntity.ok().build();
    }
}
