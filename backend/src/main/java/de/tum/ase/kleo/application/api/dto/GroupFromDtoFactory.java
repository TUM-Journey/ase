package de.tum.ase.kleo.application.api.dto;

import de.tum.ase.kleo.domain.Group;
import de.tum.ase.kleo.domain.SessionType;
import de.tum.ase.kleo.domain.id.UserId;
import lombok.val;
import org.springframework.stereotype.Component;

@Component
public class GroupFromDtoFactory {

    public Group create(GroupDTO groupDTO) {
        if (groupDTO == null)
            return null;

        val group = new Group(groupDTO.getName());

        groupDTO.getStudentIds().forEach(sId -> group.addStudent(UserId.of(sId)));
        groupDTO.getTutorIds().forEach(sId -> group.addTutor(UserId.of(sId)));

        groupDTO.getSessions().forEach(sessDto -> {
            group.addSession(SessionType.valueOf(sessDto.getType().toString()),
                    sessDto.getLocation(), sessDto.getBegins(), sessDto.getEnds());
        });

        return group;
    }
}
