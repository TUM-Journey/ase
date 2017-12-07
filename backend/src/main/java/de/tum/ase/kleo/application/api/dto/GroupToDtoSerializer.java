package de.tum.ase.kleo.application.api.dto;

import de.tum.ase.kleo.domain.Group;
import de.tum.ase.kleo.domain.id.Identifier;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

@Component
public class GroupToDtoSerializer {

    private final SessionToDtoSerializer sessionToDtoSerializer;

    @Autowired
    public GroupToDtoSerializer(SessionToDtoSerializer sessionToDtoSerializer) {
        this.sessionToDtoSerializer = sessionToDtoSerializer;
    }

    public GroupDTO toDto(Group source) {
        return toDto(source, true);
    }

    public GroupDTO toDto(Group source, boolean includeSessions) {
        if (source == null)
            return null;

        val dto = new GroupDTO()
                .id(source.id().toString())
                .name(source.name())
                .studentIds(source.studentIds().stream().map(Identifier::toString).collect(toList()))
                .tutorIds(source.tutorIds().stream().map(Identifier::toString).collect(toList()));

        if (includeSessions)
            dto.sessions(sessionToDtoSerializer.toDto(source.sessions()));

        return dto;
    }

    public List<GroupDTO> toDto(Iterable<Group> sources) {
        return toDto(sources, true);
    }

    public List<GroupDTO> toDto(Iterable<Group> sources, boolean includeSessions) {
        return stream(sources.spliterator(), false).map(s -> toDto(s, includeSessions)).collect(toList());
    }
}
