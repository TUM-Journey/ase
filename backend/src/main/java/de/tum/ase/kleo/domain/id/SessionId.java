package de.tum.ase.kleo.domain.id;

import eu.socialedge.ddd.domain.id.Identifier;

import javax.persistence.*;
import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Embeddable @Access(AccessType.FIELD)
@AttributeOverride(name = "value", column = @Column(name = "session_id"))
public class SessionId extends Identifier<String> {

    public SessionId() {
        super(UUID.randomUUID().toString());
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