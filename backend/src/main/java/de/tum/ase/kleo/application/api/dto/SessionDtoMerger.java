package de.tum.ase.kleo.application.api.dto;

import de.tum.ase.kleo.domain.Session;
import org.springframework.stereotype.Component;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Component
public class SessionDtoMerger implements DtoMerger<SessionDTO, Session> {

    @Override
    public void merge(SessionDTO dto, Session dest) {
        if (dto == null)
            return;

        if (isNotBlank(dto.getLocation()))
            dest.location(dto.getLocation());

        if (dto.getBegins() != null)
            dest.begins(dto.getBegins());

        if (dto.getEnds() != null)
            dest.ends(dto.getEnds());
    }
}
