package de.tum.ase.kleo.application.api.dto;

import org.springframework.stereotype.Component;

import de.tum.ase.kleo.domain.Group;
import de.tum.ase.kleo.domain.id.UserId;
import lombok.val;

@Component
public class GroupFromDtoFactory {

    public Group create(GroupDTO groupDTO) {
        if (groupDTO == null)
            return null;

        val group = new Group(groupDTO.getName());

        if (groupDTO.getStudentIds() != null && !groupDTO.getStudentIds().isEmpty())
            groupDTO.getStudentIds().forEach(sId -> group.addStudent(UserId.of(sId)));

        return group;
    }
}
