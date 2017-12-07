package de.tum.ase.kleo.domain.id;

import javax.persistence.*;

import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Embeddable @Access(AccessType.FIELD)
@AttributeOverride(name = "id", column = @Column(name = "group_id"))
public class GroupId extends Identifier {

    public GroupId() {
        this(UUID.randomUUID().toString());
    }

    public GroupId(String id) {
        super(id);
    }

    public static GroupId of(String id) {
        if (isBlank(id))
            return null;

        return new GroupId(id);
    }
}
