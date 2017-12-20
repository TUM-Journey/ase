package de.tum.ase.kleo.domain;

import org.junit.Before;
import org.junit.Test;

import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import de.tum.ase.kleo.domain.id.SessionId;
import de.tum.ase.kleo.domain.id.UserId;
import lombok.val;

import static org.junit.Assert.assertEquals;

public class PassTest {

    private SecretKeySpec key;
    private Cipher cipher;
    private AlgorithmParameterSpec algParamSpec;

    @Before
    public void initCipherAndKey() throws NoSuchPaddingException, NoSuchAlgorithmException {
        val uuidBytes = UUID.randomUUID().toString().getBytes();
        val keyBytes = Arrays.copyOf(uuidBytes, 16);
        val iv = new byte[]{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

        key = new SecretKeySpec(keyBytes, "AES");
        cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        algParamSpec = new IvParameterSpec(iv);
    }

    @Test
    public void fromBytesToBytedPassCorrectly() {
        val pass = randomPass();
        val passBytes = pass.toBytes();

        passBytes.flip();
        val passDecoded = Pass.fromBytes(passBytes);

        assertPasses(pass, passDecoded);
    }

    @Test
    public void decodesEncryptedTokenToPassCorrectly() {
        val pass = randomPass();

        val tokenBuilder = new Pass.Tokenizer().cipher(cipher).key(key).algParamSpec(algParamSpec)
                .sessionId(pass.sessionId())
                .studentId(pass.studentId())
                .requestedAt(pass.requestedAt())
                .expiresAt(pass.expiresAt());
        val encryptedPassTokenizedToBuffer = tokenBuilder.buildToken();

        val decryptedPass = new Pass.Decoder().cipher(cipher).key(key).algParamSpec(algParamSpec)
                .token(encryptedPassTokenizedToBuffer)
                .decode();

        assertPasses(pass, decryptedPass);
    }

    private void assertPasses(Pass expected, Pass actual) {
        assertEquals(expected.sessionId(), actual.sessionId());
        assertEquals(expected.studentId(), actual.studentId());
        assertEquals(expected.requestedAt(), actual.requestedAt());
        assertEquals(expected.expiresAt(), actual.expiresAt());
    }

    private static Pass randomPass() {
        return new Pass(new SessionId(), new UserId(), Duration.ofHours(2));
    }

}