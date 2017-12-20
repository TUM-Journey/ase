package de.tum.ase.kleo.domain.id;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.EqualsAndHashCode;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;
import java.nio.charset.Charset;

import static org.apache.commons.lang3.Validate.notBlank;

@EqualsAndHashCode(callSuper = false)
@MappedSuperclass @Access(AccessType.FIELD)
public abstract class Identifier implements Serializable {

    @Column(name = "id", nullable = false)
    protected final String id;

    @JsonCreator
    protected Identifier(String id) {
        this.id = notBlank(id);
    }

    @JsonValue
    @Override
    public String toString() {
        return id;
    }

    public byte[] toBytes(Charset charset) {
        return id.getBytes(charset);
    }
}
