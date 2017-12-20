package de.tum.ase.kleo.application.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import lombok.val;

@Configuration
public class PassTokenizationServiceConfig {

    @Value("${security.passes.keyAlgorithm}")
    private String passTokenizationKeyAlgorithm;

    @Value("${security.passes.key}")
    private String[] passTokenizationKeyBytes;

    @Value("${security.passes.cipherAlgorithm}")
    private String passTokenizationCipherAlgorithm;

    @Value("${security.passes.cipherAlgorithmParameterSpec}")
    private String[] passTokenizationCipherAlgorithmParameterSpecBytes;

    @Bean
    PassTokenizationService passTokenizationService() throws NoSuchPaddingException, NoSuchAlgorithmException {
        val cipher = Cipher.getInstance(passTokenizationCipherAlgorithm);

        val keyBytes = parseBytes(passTokenizationKeyBytes);
        val key = new SecretKeySpec(keyBytes, "AES");

        val algParamSpecBytes = parseBytes(passTokenizationCipherAlgorithmParameterSpecBytes);
        val algParamSpec = new IvParameterSpec(algParamSpecBytes);

        return new PassTokenizationService(cipher, key, algParamSpec);
    }

    private byte[] parseBytes(String[] bytesAsStrings) {
        val bytes = new byte[bytesAsStrings.length];

        for (int i = 0; i < bytesAsStrings.length; i++) {
            bytes[i] = Byte.parseByte(bytesAsStrings[i]);
        }

        return bytes;
    }
}
