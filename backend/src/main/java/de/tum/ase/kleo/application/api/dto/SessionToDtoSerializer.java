package de.tum.ase.kleo.application.api.dto;

import de.tum.ase.kleo.domain.Session;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

@Component
public class SessionToDtoSerializer {

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

    public List<SessionDTO> toDto(Iterable<Session> sources) {
        return stream(sources.spliterator(), false).map(this::toDto).collect(toList());
    }
}
