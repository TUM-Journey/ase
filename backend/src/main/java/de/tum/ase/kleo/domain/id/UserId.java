package de.tum.ase.kleo.domain.id;

import eu.socialedge.ddd.domain.id.Identifier;

import javax.persistence.*;
import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.isBlank;

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
        if (isBlank(id))
            return null;

        return new UserId(id);
    }
}