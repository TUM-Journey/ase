package de.tum.ase.kleo.application.api.mapping;

import de.tum.ase.kleo.application.api.dto.AttendanceDTO;
import de.tum.ase.kleo.application.api.dto.SessionDTO;
import de.tum.ase.kleo.domain.Pass;
import de.tum.ase.kleo.domain.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserAttendanceDtoMapper implements Mapper<Pass, AttendanceDTO> {

    private final Mapper<Session, SessionDTO> sessionDtoMapper;

    @Autowired
    public UserAttendanceDtoMapper(Mapper<Session, SessionDTO> sessionDtoMapper) {
        this.sessionDtoMapper = sessionDtoMapper;
    }

    @Override
    public AttendanceDTO map(Pass source) {
        if (source == null)
            return null;

        if (!source.isUsed())
            throw new MappingException("The Pass given has not being used. Cannot extract Attendance DTO");

        return new AttendanceDTO()
                .passId(source.code())
                .session(sessionDtoMapper.map(source.session()))
                .timestamp(source.usedDateTime().toOffsetDateTime());
    }
}
