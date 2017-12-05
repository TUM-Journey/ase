package de.tum.ase.kleo.application.api.dto;

import de.tum.ase.kleo.domain.Course;
import de.tum.ase.kleo.domain.Group;
import de.tum.ase.kleo.domain.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
public class AttendanceDtoFactory {

    private final CourseDtoMapper courseDtoMapper;
    private final GroupDtoMapper groupDtoMapper;
    private final SessionDtoMapper sessionDtoMapper;

    @Autowired
    public AttendanceDtoFactory(CourseDtoMapper courseDtoMapper, GroupDtoMapper groupDtoMapper,
                                SessionDtoMapper sessionDtoMapper) {
        this.courseDtoMapper = courseDtoMapper;
        this.groupDtoMapper = groupDtoMapper;
        this.sessionDtoMapper = sessionDtoMapper;
    }

    public AttendanceDTO create(Course course, Group group, Session session, OffsetDateTime attendedAt) {
        return new AttendanceDTO()
                .course(courseDtoMapper.toDto(course))
                .group(groupDtoMapper.toDto(group))
                .session(sessionDtoMapper.toDto(session))
                .attendedAt(attendedAt);
    }
}
