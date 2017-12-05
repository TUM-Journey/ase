package de.tum.ase.kleo.application.api.dto;

import de.tum.ase.kleo.domain.Course;
import de.tum.ase.kleo.domain.id.CourseId;
import de.tum.ase.kleo.domain.id.GroupId;
import org.springframework.stereotype.Component;

import java.util.Collections;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

@Component
public class CourseDtoMapper implements DtoSerializer<Course, CourseDTO>,
        DtoDeserializer<CourseDTO, Course> {

    @Override
    public Course fromDto(CourseDTO dto) {
        if (dto == null)
            return null;

        return new Course(CourseId.of(dto.getId()),
                dto.getName(),
                dto.getDescription(),
                defaultIfNull(dto.getGroupIds(), Collections.<String>emptyList())
                        .stream().map(GroupId::of).collect(toSet()));
    }

    @Override
    public CourseDTO toDto(Course source) {
        if (source == null)
            return null;

        return new CourseDTO()
                .id(source.id().toString())
                .name(source.name())
                .description(source.description())
                .groupIds(source.groupIds().stream().map(GroupId::toString).collect(toList()));
    }
}
