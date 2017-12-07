package de.tum.ase.kleo.application.api.dto;

import de.tum.ase.kleo.domain.Session;
import org.springframework.stereotype.Component;

@Component
public class SessionDtoSerializer implements DtoSerializer<Session, SessionDTO> {

    @Override
    public SessionDTO toDto(Session source) {
        if (source == null)
            return null;

        return new SessionDTO()
                .id(source.id().toString())
                .type(SessionDTO.TypeEnum.fromValue(source.sessionType().name()))
                .location(source.location())
                .begins(source.begins())
                .ends(source.ends());
    }
}
