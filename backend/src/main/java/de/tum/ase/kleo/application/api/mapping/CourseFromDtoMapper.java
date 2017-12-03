package de.tum.ase.kleo.application.api.mapping;

import de.tum.ase.kleo.application.api.dto.CourseDTO;
import de.tum.ase.kleo.domain.Course;
import org.springframework.stereotype.Component;

@Component
public class CourseFromDtoMapper implements Mapper<CourseDTO, Course> {

    @Override
    public Course map(CourseDTO source) {
        if (source == null)
            return null;

        return new Course(source.getId(), source.getName(), source.getDescription());
    }
}
