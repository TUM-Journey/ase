package de.tum.ase.kleo.application.service;

import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import de.tum.ase.kleo.domain.Pass;

@Component
public class PassTokenizationService {

    private final Cipher cipher;

    private final SecretKeySpec key;

    private final AlgorithmParameterSpec algParamSpec;

    public PassTokenizationService(Cipher cipher, SecretKeySpec key, AlgorithmParameterSpec algParamSpec) {
        this.cipher = cipher;
        this.key = key;
        this.algParamSpec = algParamSpec;
    }

    public ByteBuffer tokenize(Pass pass) {
        return pass.toToken(cipher, key, algParamSpec);
    }

    public String tokenizeToString(Pass pass) {
        return pass.toTokenString(cipher, key, algParamSpec);
    }

    public Pass untokenize(ByteBuffer token) {
        return new Pass.Decoder().cipher(cipher).key(key).algParamSpec(algParamSpec)
                .token(token).decode();
    }

    public Pass untokenize(String tokenString) {
        return new Pass.Decoder().cipher(cipher).key(key).algParamSpec(algParamSpec)
                .token(tokenString).decode();
    }
}
