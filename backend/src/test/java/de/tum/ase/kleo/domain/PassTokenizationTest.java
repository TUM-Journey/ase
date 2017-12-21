package de.tum.ase.kleo.domain;

import org.junit.Before;
import org.junit.Test;

import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.time.Duration;
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

public class PassTokenizationTest {

    private PassTokenizer passTokenizer;
    private PassDetokenizer passDetokenizer;

    @Before
    public void initCipherAndKey() throws NoSuchPaddingException, NoSuchAlgorithmException {
        val uuidBytes = UUID.randomUUID().toString().getBytes();
        val keyBytes = Arrays.copyOf(uuidBytes, 16);
        val iv = new byte[]{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

        val key = new SecretKeySpec(keyBytes, "AES");
        val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        val algParamSpec = new IvParameterSpec(iv);

        passTokenizer = new PassTokenizer(cipher, key, algParamSpec);
        passDetokenizer = new PassDetokenizer(cipher, key, algParamSpec);
    }

    @Test
    public void decodesEncryptedTokenToPassCorrectly() {
        val pass = new Pass(new SessionId(), new UserId(), Duration.ofHours(2));
        
        val encryptedPassTokenizedToBuffer = passTokenizer.tokenize(pass);
        val decryptedPass = passDetokenizer.detokenize(encryptedPassTokenizedToBuffer);

        assertEquals(pass.sessionId(), decryptedPass.sessionId());
        assertEquals(pass.studentId(), decryptedPass.studentId());
        assertEquals(pass.requestedAt(), decryptedPass.requestedAt());
        assertEquals(pass.expiresAt(), decryptedPass.expiresAt());
    }
}
