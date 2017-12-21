package de.tum.ase.kleo.domain;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import lombok.val;

import static de.tum.ase.kleo.domain.Pass.DEFAULT_TEXT_CHARSET;
import static org.apache.commons.lang3.Validate.notNull;

public class PassTokenizer {

    private final Cipher cipher;

    private final SecretKeySpec key;

    private final AlgorithmParameterSpec algParamSpec;

    private final Charset charset;

    public PassTokenizer(Cipher cipher, SecretKeySpec key, AlgorithmParameterSpec algParamSpec, Charset charset) {
        this.cipher = notNull(cipher);
        this.key = notNull(key);
        this.algParamSpec = notNull(algParamSpec);
        this.charset = notNull(charset);
    }

    public PassTokenizer(Cipher cipher, SecretKeySpec key, AlgorithmParameterSpec algParamSpec) {
        this(cipher, key, algParamSpec, DEFAULT_TEXT_CHARSET);
    }

    public ByteBuffer tokenize(Pass pass) {
        try {
            cipher.init(Cipher.ENCRYPT_MODE, key, algParamSpec);

            val passBuffer = pass.toBytes(charset);
            val tokenBuffer = ByteBuffer.allocate(cipher.getOutputSize(passBuffer.position()));

            passBuffer.flip();
            cipher.doFinal(passBuffer, tokenBuffer);

            tokenBuffer.flip();
            return tokenBuffer;
        } catch (GeneralSecurityException e) {
            throw new PassTokenizationException("Failed to tokenize the Pass", e);
        }
    }

    public byte[] tokenizeToBytes(Pass pass) {
        val passBuffer = tokenize(pass);

        val passBytes = new byte[passBuffer.remaining()];
        passBuffer.get(passBytes);

        return passBytes;
    }

    public String tokenizeToString(Pass pass) {
        return new String(tokenizeToBytes(pass), charset);
    }
}
