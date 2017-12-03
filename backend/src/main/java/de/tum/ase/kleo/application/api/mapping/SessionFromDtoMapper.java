package de.tum.ase.kleo.application.api.mapping;

import de.tum.ase.kleo.application.api.dto.SessionDTO;
import de.tum.ase.kleo.domain.Session;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;

@Component
public class SessionFromDtoMapper implements Mapper<SessionDTO, Session> {

    @Override
    public Session map(SessionDTO source) {
        if (source == null)
            return null;

        return new Session(source.getId(), source.getLocation(), source.getNote(),
                ZonedDateTime.from(source.getBegins()), ZonedDateTime.from(source.getEnds()));
    }
}
