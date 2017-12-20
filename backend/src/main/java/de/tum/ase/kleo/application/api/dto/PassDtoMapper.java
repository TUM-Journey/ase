package de.tum.ase.kleo.application.api.dto;

import org.springframework.stereotype.Component;

import java.time.Duration;

import de.tum.ase.kleo.application.service.PassTokenizationService;
import de.tum.ase.kleo.domain.Pass;
import de.tum.ase.kleo.domain.id.SessionId;
import de.tum.ase.kleo.domain.id.UserId;
import lombok.val;

@Component
public class PassDtoMapper {

    private final PassTokenizationService passTokenizationService;

    public PassDtoMapper(PassTokenizationService passTokenizationService) {
        this.passTokenizationService = passTokenizationService;
    }

    public PassDTO toDto(Pass source) {
        if (source == null)
            return null;

        val passCode = passTokenizationService.tokenizeToString(source);

        return new PassDTO()
                .code(passCode)
                .sessionId(source.sessionId().toString())
                .studentId(source.studentId().toString())
                .requestedAt(source.requestedAt())
                .expiresAt(source.expiresAt());
    }

    public Pass fromDto(PassDTO passDTO) {
        if (passDTO == null)
            return null;

        return new Pass(SessionId.of(passDTO.getSessionId()),
                UserId.of(passDTO.getStudentId()),
                Duration.ofSeconds(passDTO.getExpireIn()));
    }
}
