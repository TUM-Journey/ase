package de.tum.ase.kleo.domain;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import lombok.val;

import static de.tum.ase.kleo.domain.Pass.DEFAULT_TEXT_CHARSET;
import static org.apache.commons.lang3.Validate.notNull;

/**
 * {@code PassDetokenizer} domain service is responsible for {@link Pass}
 * deserialization & decryption from tokens used as identity authenticator
 * for attendance registering by tutor’s device.
 */
public class PassDetokenizer {

    private final Cipher cipher;

    private final SecretKeySpec key;

    private final AlgorithmParameterSpec algParamSpec;

    private final Charset charset;

    public PassDetokenizer(Cipher cipher, SecretKeySpec key, AlgorithmParameterSpec algParamSpec, Charset charset) {
        this.cipher = notNull(cipher);
        this.key = notNull(key);
        this.algParamSpec = notNull(algParamSpec);
        this.charset = notNull(charset);
    }

    public PassDetokenizer(Cipher cipher, SecretKeySpec key, AlgorithmParameterSpec algParamSpec) {
        this(cipher, key, algParamSpec, DEFAULT_TEXT_CHARSET);
    }

    public Pass detokenize(ByteBuffer tokenBuffer) {
        try {
            cipher.init(Cipher.DECRYPT_MODE, key, algParamSpec);

            val passBuffer = ByteBuffer.allocate(cipher.getOutputSize(tokenBuffer.remaining()));
            cipher.doFinal(tokenBuffer, passBuffer);

            passBuffer.flip();
            return Pass.fromBytes(passBuffer, charset);
        } catch (GeneralSecurityException e) {
            throw new PassTokenizationException("Failed to decode a Pass from the token", e);
        }
    }

    public Pass detokenize(byte[] bytes) {
        return detokenize(ByteBuffer.wrap(bytes));
    }

    public Pass detokenize(String tokenString) {
        try {
            return detokenize(Hex.decodeHex(tokenString.toCharArray()));
        } catch (DecoderException e) {
            throw new PassTokenizationException("Failed to decode hexed token", e);
        }
    }
}
