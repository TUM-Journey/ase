package de.tum.ase.kleo.application.api.dto;

import de.tum.ase.kleo.domain.Attendance;
import de.tum.ase.kleo.domain.Group;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

@Component
public class AttendanceDtoSerializer {

    private final GroupDtoSerializer groupDtoSerializer;
    private final SessionDtoSerializer sessionDtoSerializer;

    @Autowired
    public AttendanceDtoSerializer(GroupDtoSerializer groupDtoSerializer, SessionDtoSerializer sessionDtoSerializer) {
        this.groupDtoSerializer = groupDtoSerializer;
        this.sessionDtoSerializer = sessionDtoSerializer;
    }

    public AttendanceDTO toDto(Group group, Attendance attendance) {
        if (attendance == null)
            return null;

        return new AttendanceDTO()
                .session(sessionDtoSerializer.toDto(group.session(attendance.sessionId()).get()))
                .group(groupDtoSerializer.toDto(group, false))
                .attendedAt(attendance.attendedAt())
                .passCode(attendance.passCode().toString());
    }

    public List<AttendanceDTO> toDto(Group group, Iterable<Attendance> sources) {
        return stream(sources.spliterator(), false).map(s -> toDto(group, s)).collect(toList());
    }
}
