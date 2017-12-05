package de.tum.ase.kleo.domain.v2.id;

import eu.socialedge.ddd.domain.id.Identifier;

import javax.persistence.*;
import java.util.UUID;

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
        return new CourseId(id);
    }
}