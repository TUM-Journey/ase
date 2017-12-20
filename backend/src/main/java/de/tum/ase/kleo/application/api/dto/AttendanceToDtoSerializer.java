package de.tum.ase.kleo.application.api.dto;

import de.tum.ase.kleo.domain.Attendance;
import de.tum.ase.kleo.domain.Group;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

@Component
public class AttendanceToDtoSerializer {

    private final GroupToDtoSerializer groupToDtoSerializer;
    private final SessionToDtoSerializer sessionToDtoSerializer;

    @Autowired
    public AttendanceToDtoSerializer(GroupToDtoSerializer groupToDtoSerializer,
                                     SessionToDtoSerializer sessionToDtoSerializer) {
        this.groupToDtoSerializer = groupToDtoSerializer;
        this.sessionToDtoSerializer = sessionToDtoSerializer;
    }

    public AttendanceDTO toDto(Group group, Attendance attendance) {
        if (attendance == null)
            return null;

        return new AttendanceDTO()
                .session(sessionToDtoSerializer.toDto(group.session(attendance.sessionId()).get()))
                .group(groupToDtoSerializer.toDto(group, false))
                .attendedAt(attendance.attendedAt());
    }

    public List<AttendanceDTO> toDto(Group group, Iterable<Attendance> sources) {
        return stream(sources.spliterator(), false).map(s -> toDto(group, s)).collect(toList());
    }
}
