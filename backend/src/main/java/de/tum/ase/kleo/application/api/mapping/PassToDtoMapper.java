package de.tum.ase.kleo.application.api.mapping;

import de.tum.ase.kleo.application.api.dto.PassDTO;
import de.tum.ase.kleo.application.api.dto.SessionDTO;
import de.tum.ase.kleo.domain.Pass;
import de.tum.ase.kleo.domain.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PassToDtoMapper implements Mapper<Pass, PassDTO> {

    private final Mapper<Session, SessionDTO> sessionDtoMapper;

    @Autowired
    public PassToDtoMapper(Mapper<Session, SessionDTO> sessionDtoMapper) {
        this.sessionDtoMapper = sessionDtoMapper;
    }

    @Override
    public PassDTO map(Pass source) {
        if (source == null)
            return null;

        return new PassDTO()
                .id(source.id())
                .session(sessionDtoMapper.map(source.session()))
                .generated(source.generatedDateTime().toOffsetDateTime());
    }
}
