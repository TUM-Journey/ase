package de.tum.ase.kleo.application.api.dto;

import de.tum.ase.kleo.domain.Group;
import de.tum.ase.kleo.domain.id.GroupId;
import de.tum.ase.kleo.domain.id.SessionId;
import de.tum.ase.kleo.domain.id.UserId;
import org.springframework.stereotype.Component;

import java.util.Collections;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

@Component
public class GroupDtoMapper implements DtoSerializer<Group, GroupDTO>,
        DtoDeserializer<GroupDTO, Group> {

    @Override
    public Group fromDto(GroupDTO dto) {
        if (dto == null)
            return null;

        return new Group(GroupId.of(dto.getId()),
                dto.getName(),
                defaultIfNull(dto.getStudentIds(), Collections.<String>emptyList())
                        .stream().map(UserId::of).collect(toSet()),
                defaultIfNull(dto.getTutorIds(), Collections.<String>emptyList())
                        .stream().map(UserId::of).collect(toSet()),
                defaultIfNull(dto.getSessionIds(), Collections.<String>emptyList())
                        .stream().map(SessionId::of).collect(toSet()));
    }

    @Override
    public GroupDTO toDto(Group source) {
        if (source == null)
            return null;

        return new GroupDTO()
                .id(source.id().toString())
                .name(source.name())
                .studentIds(source.studentIds().stream().map(UserId::toString).collect(toList()))
                .tutorIds(source.tutorIds().stream().map(UserId::toString).collect(toList()))
                .sessionIds(source.sessionIds().stream().map(SessionId::toString).collect(toList()));
    }
}
