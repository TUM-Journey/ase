package de.tum.ase.kleo.application.api.mapping;

import de.tum.ase.kleo.application.api.dto.SessionDTO;
import de.tum.ase.kleo.domain.Session;
import org.springframework.stereotype.Component;

@Component
public class SessionToDtoMapper implements Mapper<Session, SessionDTO> {

    @Override
    public SessionDTO map(Session source) {
        if (source == null)
            return null;

        return new SessionDTO()
                .id(source.id())
                .location(source.location())
                .note(source.note())
                .begins(source.begins())
                .ends(source.begins());
    }
}
