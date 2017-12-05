package de.tum.ase.kleo.domain.v2.id;

import eu.socialedge.ddd.domain.id.Identifier;

import javax.persistence.*;
import java.util.UUID;

@Embeddable @Access(AccessType.FIELD)
@AttributeOverride(name = "value", column = @Column(name = "user_id"))
public class UserId extends Identifier<String> {

    public UserId() {
        super(UUID.randomUUID().toString());
    }

    public UserId(String id) {
        super(id);
    }

    public static UserId of(String id) {
        return new UserId(id);
    }
}