package de.tum.ase.kleo.application.config.domain;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import de.tum.ase.kleo.domain.PassDetokenizer;
import de.tum.ase.kleo.domain.PassTokenizer;
import lombok.val;

@Configuration
public class PassTokenizationConfig {

    private final Cipher cipher;
    private final SecretKeySpec key;
    private final AlgorithmParameterSpec algParamSpec;

    public PassTokenizationConfig(@Value("${security.passes.keyAlgorithm}") String keyAlg,
                                  @Value("${security.passes.key}") String[] keyBytes,
                                  @Value("${security.passes.cipherAlgorithm}") String cipherAlg,
                                  @Value("${security.passes.cipherAlgorithmParameterSpec}") String[] cipherAlgParams)
            throws NoSuchPaddingException, NoSuchAlgorithmException {

        cipher = Cipher.getInstance(cipherAlg);
        key = new SecretKeySpec(parseBytes(keyBytes), keyAlg);
        algParamSpec = new IvParameterSpec(parseBytes(cipherAlgParams));
    }

    @Bean
    PassTokenizer passTokenizer() throws NoSuchPaddingException, NoSuchAlgorithmException {
        return new PassTokenizer(cipher, key, algParamSpec);
    }

    @Bean
    PassDetokenizer passDetokenizer() throws NoSuchPaddingException, NoSuchAlgorithmException {
        return new PassDetokenizer(cipher, key, algParamSpec);
    }

    private static byte[] parseBytes(String[] bytesAsStrings) {
        val bytes = new byte[bytesAsStrings.length];

        for (int i = 0; i < bytesAsStrings.length; i++) {
            bytes[i] = Byte.parseByte(bytesAsStrings[i]);
        }

        return bytes;
    }
}
