package de.tum.ase.kleo.application.api.mapping;

import de.tum.ase.kleo.application.api.dto.CourseDTO;
import de.tum.ase.kleo.domain.Course;
import org.springframework.stereotype.Component;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Component
public class CourseDtoMerger implements Merger<CourseDTO, Course> {

    @Override
    public void merge(CourseDTO source, Course dest) {
        if (isNotBlank(source.getName()))
            dest.name(source.getName());

        if (isNotBlank(source.getDescription()))
            dest.description(source.getDescription());
    }
}
