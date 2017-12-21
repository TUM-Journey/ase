package de.tum.ase.kleo.domain;

import org.junit.Test;

import java.time.Duration;

import de.tum.ase.kleo.domain.id.SessionId;
import de.tum.ase.kleo.domain.id.UserId;
import lombok.val;

import static org.junit.Assert.assertEquals;

public class PassTest {

    @Test
    public void fromBytesToBytedPassCorrectly() {
        val pass = new Pass(new SessionId(), new UserId(), Duration.ofHours(2));
        val passBytes = pass.toBytes();

        passBytes.flip();
        val passDecoded = Pass.fromBytes(passBytes);

        assertEquals(pass.sessionId(), passDecoded.sessionId());
        assertEquals(pass.studentId(), passDecoded.studentId());
        assertEquals(pass.requestedAt(), passDecoded.requestedAt());
        assertEquals(pass.expiresAt(), passDecoded.expiresAt());
    }
}