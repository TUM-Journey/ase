package de.tum.ase.kleo.domain.v2.id;

import eu.socialedge.ddd.domain.id.Identifier;

import javax.persistence.*;
import java.util.UUID;

@Embeddable @Access(AccessType.FIELD)
@AttributeOverride(name = "value", column = @Column(name = "pass_id"))
public class PassId extends Identifier<String> {

    public PassId() {
        super(UUID.randomUUID().toString());
    }

    public PassId(String id) {
        super(id);
    }

    public static PassId of(String id) {
        return new PassId(id);
    }
}