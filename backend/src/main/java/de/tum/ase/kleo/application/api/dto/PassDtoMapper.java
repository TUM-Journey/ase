package de.tum.ase.kleo.application.api.dto;

import de.tum.ase.kleo.domain.Pass;
import de.tum.ase.kleo.domain.id.SessionId;
import de.tum.ase.kleo.domain.id.UserId;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class PassDtoMapper {

    public PassDTO toDto(Pass source) {
        if (source == null)
            return null;

        return new PassDTO()
                .code(source.code().toString())
                .sessionId(source.sessionId().toString())
                .tutorId(source.tutorId().toString())
                .studentId(source.studentId().toString())
                .requestedAt(source.requestedAt())
                .expiresAt(source.expiresAt());
    }

    public Pass fromDto(PassDTO passDTO) {
        if (passDTO == null)
            return null;

        return new Pass(SessionId.of(passDTO.getSessionId()),
                UserId.of(passDTO.getTutorId()),
                UserId.of(passDTO.getStudentId()),
                Duration.ofSeconds(passDTO.getExpireIn()));
    }
}
