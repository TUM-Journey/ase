package de.tum.ase.kleo.application.api.dto;

import de.tum.ase.kleo.domain.Course;
import de.tum.ase.kleo.domain.id.GroupId;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Component
public class CourseDtoMerger implements DtoMerger<CourseDTO, Course> {

    @Override
    public void merge(CourseDTO dto, Course dest) {
        if (dto == null)
            return;

        if (isNotBlank(dto.getName()))
            dest.name(dto.getName());

        if (isNotBlank(dto.getDescription()))
            dest.description(dto.getDescription());

        if (dto.getGroupIds() != null && !dto.getGroupIds().isEmpty())
            dest.groupIds(dto.getGroupIds().stream().map(GroupId::of).collect(Collectors.toSet()));
    }
}
