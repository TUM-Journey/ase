package de.tum.ase.kleo.application.api.dto;

import de.tum.ase.kleo.domain.Group;
import de.tum.ase.kleo.domain.id.SessionId;
import de.tum.ase.kleo.domain.id.UserId;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Component
public class GroupDtoMerger implements DtoMerger<GroupDTO, Group> {

    @Override
    public void merge(GroupDTO dto, Group dest) {
        if (dto == null)
            return;

        if (isNotBlank(dto.getName()))
            dest.name(dto.getName());

        if (dto.getStudentIds() != null && !dto.getStudentIds().isEmpty())
            dest.studentIds(dto.getStudentIds().stream().map(UserId::of).collect(Collectors.toSet()));

        if (dto.getTutorIds() != null && !dto.getTutorIds().isEmpty())
            dest.tutorIds(dto.getTutorIds().stream().map(UserId::of).collect(Collectors.toSet()));

        if (dto.getSessionIds() != null && !dto.getSessionIds().isEmpty())
            dest.sessionIds(dto.getSessionIds().stream().map(SessionId::of).collect(Collectors.toSet()));
    }
}
