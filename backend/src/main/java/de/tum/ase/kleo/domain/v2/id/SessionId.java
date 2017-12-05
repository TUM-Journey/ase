package de.tum.ase.kleo.domain.v2.id;

import eu.socialedge.ddd.domain.id.Identifier;

import javax.persistence.*;
import java.util.UUID;

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
        return new SessionId(id);
    }
}