package de.tum.ase.kleo.application.api.mapping;

import de.tum.ase.kleo.application.api.dto.CourseDTO;
import de.tum.ase.kleo.domain.Course;
import org.springframework.stereotype.Component;

@Component
public class CourseToDtoMapper implements Mapper<Course, CourseDTO> {

    @Override
    public CourseDTO map(Course source) {
        if (source == null)
            return null;

        return new CourseDTO()
                .id(source.id())
                .name(source.name())
                .description(source.description())
                .begins(source.begins().get())
                .ends(source.ends().get());
    }
}
