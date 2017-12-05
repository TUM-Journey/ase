package de.tum.ase.kleo.application.api.dto;

import de.tum.ase.kleo.domain.Course;
import de.tum.ase.kleo.domain.Group;
import org.springframework.stereotype.Component;

@Component
public class TutoringDtoFactory {

    private final CourseDtoMapper courseDtoMapper;
    private final GroupDtoMapper groupDtoMapper;

    public TutoringDtoFactory(CourseDtoMapper courseDtoMapper, GroupDtoMapper groupDtoMapper) {
        this.courseDtoMapper = courseDtoMapper;
        this.groupDtoMapper = groupDtoMapper;
    }

    public TutoringDTO create(Course course, Group group) {
        return new TutoringDTO()
                .course(courseDtoMapper.toDto(course))
                .group(groupDtoMapper.toDto(group));
    }
}
