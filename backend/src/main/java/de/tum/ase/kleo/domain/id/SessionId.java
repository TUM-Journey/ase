package de.tum.ase.kleo.domain.id;

import javax.persistence.*;
import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Embeddable @Access(AccessType.FIELD)
@AttributeOverride(name = "id", column = @Column(name = "session_id"))
public class SessionId extends Identifier {

    public SessionId() {
        this(UUID.randomUUID().toString());
    }

    public SessionId(String id) {
        super(id);
    }

    public static SessionId of(String id) {
        if (isBlank(id))
            return null;

        return new SessionId(id);
    }
}
