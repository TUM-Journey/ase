package de.tum.ase.kleo.application.api.mapping;

import de.tum.ase.kleo.application.api.dto.SessionDTO;
import de.tum.ase.kleo.domain.Session;
import org.springframework.stereotype.Component;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Component
public class SessionDtoMerger implements Merger<SessionDTO, Session> {

    @Override
    public void merge(SessionDTO source, Session dest) {
        if (isNotBlank(source.getLocation()))
            dest.location(source.getLocation());

        if (isNotBlank(source.getNote()))
            dest.note(source.getNote());

        if (source.getBegins() != null)
            dest.begins(source.getBegins());

        if (source.getEnds() != null)
            dest.ends(source.getEnds());

    }
}
