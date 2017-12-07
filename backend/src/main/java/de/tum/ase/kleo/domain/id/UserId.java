package de.tum.ase.kleo.domain.id;

import javax.persistence.*;
import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Embeddable @Access(AccessType.FIELD)
@AttributeOverride(name = "id", column = @Column(name = "user_id"))
public class UserId extends Identifier {

    public UserId() {
        this(UUID.randomUUID().toString());
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
