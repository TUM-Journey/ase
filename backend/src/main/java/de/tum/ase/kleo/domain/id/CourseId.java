package de.tum.ase.kleo.domain.id;

import eu.socialedge.ddd.domain.id.Identifier;

import javax.persistence.*;
import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Embeddable @Access(AccessType.FIELD)
@AttributeOverride(name = "value", column = @Column(name = "course_id"))
public class CourseId extends Identifier<String> {

    public CourseId() {
        super(UUID.randomUUID().toString());
    }

    public CourseId(String id) {
        super(id);
    }

    public static CourseId of(String id) {
        if (isBlank(id))
            return null;

        return new CourseId(id);
    }
}