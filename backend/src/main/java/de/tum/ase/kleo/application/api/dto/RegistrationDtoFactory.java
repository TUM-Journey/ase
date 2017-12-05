package de.tum.ase.kleo.application.api.dto;

import de.tum.ase.kleo.domain.Course;
import de.tum.ase.kleo.domain.Group;
import org.springframework.stereotype.Component;

@Component
public class RegistrationDtoFactory {

    private final CourseDtoMapper courseDtoMapper;
    private final GroupDtoMapper groupDtoMapper;

    public RegistrationDtoFactory(CourseDtoMapper courseDtoMapper, GroupDtoMapper groupDtoMapper) {
        this.courseDtoMapper = courseDtoMapper;
        this.groupDtoMapper = groupDtoMapper;
    }

    public RegistrationDTO create(Course course, Group group) {
        return new RegistrationDTO()
                .course(courseDtoMapper.toDto(course))
                .group(groupDtoMapper.toDto(group));
    }
}
