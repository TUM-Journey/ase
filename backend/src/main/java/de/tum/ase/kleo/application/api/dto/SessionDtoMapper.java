package de.tum.ase.kleo.application.api.dto;

import de.tum.ase.kleo.domain.Session;
import de.tum.ase.kleo.domain.id.SessionId;
import org.springframework.stereotype.Component;

@Component
public class SessionDtoMapper implements DtoSerializer<Session, SessionDTO>,
        DtoDeserializer<SessionDTO, Session> {

    @Override
    public Session fromDto(SessionDTO dto) {
        if (dto == null)
            return null;

        return new Session(SessionId.of(dto.getId()),
                dto.getLocation(),
                dto.getBegins(),
                dto.getEnds());
    }

    @Override
    public SessionDTO toDto(Session source) {
        if (source == null)
            return null;

        return new SessionDTO()
                .id(source.id().toString())
                .location(source.location())
                .begins(source.begins())
                .ends(source.ends());
    }
}
