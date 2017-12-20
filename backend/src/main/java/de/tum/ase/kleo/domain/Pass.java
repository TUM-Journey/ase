package de.tum.ase.kleo.domain;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.spec.AlgorithmParameterSpec;
import java.time.Duration;
import java.time.OffsetDateTime;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import de.tum.ase.kleo.domain.id.SessionId;
import de.tum.ase.kleo.domain.id.UserId;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.val;

import static org.apache.commons.lang3.Validate.notNull;

@ToString @EqualsAndHashCode
@Getter @Accessors(fluent = true)
public class Pass {

    private final static Charset DEFAULT_TEXT_CHARSET = Charset.forName("UTF-8");

    private final static Duration DEFAULT_EXPIRE = Duration.ofMinutes(15);

    private final SessionId sessionId;

    private final UserId studentId;

    private final OffsetDateTime requestedAt;

    private final OffsetDateTime expiresAt;

    private Pass(SessionId sessionId, UserId studentId, OffsetDateTime requestedAt, OffsetDateTime expiresAt) {
        this.sessionId = notNull(sessionId);
        this.studentId = notNull(studentId);
        this.requestedAt = requestedAt == null ? OffsetDateTime.now() : requestedAt;
        this.expiresAt = expiresAt == null ? this.requestedAt.plus(DEFAULT_EXPIRE) : expiresAt;
    }

    private Pass(SessionId sessionId, UserId studentId, OffsetDateTime requestedAt, Duration expiresIn) {
        this(sessionId, studentId,
                requestedAt == null ? OffsetDateTime.now() : requestedAt,
                requestedAt == null ? OffsetDateTime.now().plus(expiresIn) : requestedAt.plus(expiresIn));
    }

    public Pass(SessionId sessionId, UserId studentId, Duration expiresIn) {
        this(sessionId, studentId, OffsetDateTime.now(), expiresIn);
    }

    public Pass(SessionId sessionId, UserId studentId) {
        this(sessionId, studentId, DEFAULT_EXPIRE);
    }

    public boolean isExpired() {
        return OffsetDateTime.now().isAfter(expiresAt);
    }

    public boolean notExpired() {
        return !isExpired();
    }

    public ByteBuffer toToken(Cipher cipher, SecretKeySpec key, AlgorithmParameterSpec algParamSpec, Charset charset) {
        try {
            cipher.init(Cipher.ENCRYPT_MODE, key, algParamSpec);

            val passBuffer = toBytes(charset);
            val tokenBuffer = ByteBuffer.allocate(cipher.getOutputSize(passBuffer.position()));

            passBuffer.flip();
            cipher.doFinal(passBuffer, tokenBuffer);

            tokenBuffer.flip();
            return tokenBuffer;
        } catch (GeneralSecurityException e) {
            throw new PassTokenizationException("Failed to tokenize the Pass", e);
        }
    }

    public ByteBuffer toToken(Cipher cipher, SecretKeySpec key, AlgorithmParameterSpec algParamSpec) {
        return toToken(cipher, key, algParamSpec, DEFAULT_TEXT_CHARSET);
    }

    public String toTokenString(Cipher cipher, SecretKeySpec key, AlgorithmParameterSpec algParamSpec,
                                Charset charset) {
        val tokenBuffer = toToken(cipher, key, algParamSpec, charset);
        return new String(bytesFromBuffer(tokenBuffer), charset);
    }

    public String toTokenString(Cipher cipher, SecretKeySpec key, AlgorithmParameterSpec algParamSpec) {
        return toTokenString(cipher, key, algParamSpec, DEFAULT_TEXT_CHARSET);
    }

    public static Pass fromToken(Cipher cipher, SecretKeySpec key, AlgorithmParameterSpec algParamSpec,
                                 ByteBuffer tokenBuffer, Charset charset) {
        try {
            cipher.init(Cipher.DECRYPT_MODE, key, algParamSpec);

            val passBuffer = ByteBuffer.allocate(cipher.getOutputSize(tokenBuffer.remaining()));
            cipher.doFinal(tokenBuffer, passBuffer);

            passBuffer.flip();
            return fromBytes(passBuffer, charset);
        } catch (GeneralSecurityException e) {
            throw new PassTokenizationException("Failed to decode a Pass from the token", e);
        }
    }

    public static Pass fromToken(Cipher cipher, SecretKeySpec key, AlgorithmParameterSpec algParamSpec,
                                 ByteBuffer tokenBuffer) {
        return fromToken(cipher, key, algParamSpec, tokenBuffer, DEFAULT_TEXT_CHARSET);
    }

    public static Pass fromTokenString(Cipher cipher, SecretKeySpec key, AlgorithmParameterSpec algParamSpec,
                                       String tokenString, Charset charset) {
        return fromToken(cipher, key, algParamSpec, ByteBuffer.wrap(tokenString.getBytes(charset)), charset);
    }

    public static Pass fromTokenString(Cipher cipher, SecretKeySpec key, AlgorithmParameterSpec algParamSpec,
                                       String tokenString) {
        return fromTokenString(cipher, key, algParamSpec, tokenString, DEFAULT_TEXT_CHARSET);
    }

    public ByteBuffer toBytes(Charset charset) {
        val sessionIdBytes = sessionId.toBytes(charset);
        val sessionIdBytesLength = sessionIdBytes.length;

        val studentIdBytes = studentId.toBytes(charset);
        val studentIdBytesLength = studentIdBytes.length;

        val requestedAtBytes = requestedAt.toString().getBytes(charset);
        val requestedAtBytesLength = requestedAtBytes.length;

        val expiresAtBytes = expiresAt.toString().getBytes(charset);
        val expiresAtBytesLength = expiresAtBytes.length;


        val passBuffer = ByteBuffer.allocate(
                Integer.BYTES + Integer.BYTES + // sessionIdBytesLength && studentIdBytesLength
                        sessionIdBytes.length + studentIdBytes.length +
                        Integer.BYTES + Integer.BYTES + // requestedAtBytesLength && expiresAtBytesLength
                        requestedAtBytes.length + expiresAtBytes.length);
        passBuffer
                .putInt(sessionIdBytesLength)
                .put(sessionIdBytes)

                .putInt(studentIdBytesLength)
                .put(studentIdBytes)

                .putInt(requestedAtBytesLength)
                .put(requestedAtBytes)

                .putInt(expiresAtBytesLength)
                .put(expiresAtBytes);

        return passBuffer;
    }

    public ByteBuffer toBytes() {
        return toBytes(DEFAULT_TEXT_CHARSET);
    }

    public static Pass fromBytes(ByteBuffer token, Charset charset) {
        val sessionIdBytesLength = token.getInt();
        val sessionIdBytes = new byte[sessionIdBytesLength];
        token.get(sessionIdBytes);

        val studentIdBytesLength = token.getInt();
        val studentIdBytes = new byte[studentIdBytesLength];
        token.get(studentIdBytes);

        val requestedAtBytesLength = token.getInt();
        val requestedAtBytes = new byte[requestedAtBytesLength];
        token.get(requestedAtBytes);

        val expiresAtBytesLength = token.getInt();
        val expiresAtBytes = new byte[expiresAtBytesLength];
        token.get(expiresAtBytes);

        val sessionId = SessionId.of(new String(sessionIdBytes, charset));
        val studentId = UserId.of(new String(studentIdBytes, charset));
        val requestedAt = OffsetDateTime.parse(new String(requestedAtBytes, charset));
        val expiresAt = OffsetDateTime.parse(new String(expiresAtBytes, charset));

        return new Pass(sessionId, studentId, requestedAt, expiresAt);
    }

    public static Pass fromBytes(ByteBuffer token) {
        return fromBytes(token, DEFAULT_TEXT_CHARSET);
    }

    private static byte[] bytesFromBuffer(ByteBuffer byteBuffer) {
        val bytes = new byte[byteBuffer.remaining()];
        byteBuffer.get(bytes);

        return bytes;
    }

    public static final class Tokenizer {

        private Charset charset = DEFAULT_TEXT_CHARSET;

        private Cipher cipher;

        private SecretKeySpec key;

        private AlgorithmParameterSpec algParamSpec;

        private SessionId sessionId;

        private UserId studentId;

        private OffsetDateTime requestedAt;

        private OffsetDateTime expiresAt;

        public Tokenizer charset(Charset charset) {
            this.charset = charset;
            return this;
        }

        public Tokenizer cipher(Cipher cipher) {
            this.cipher = cipher;
            return this;
        }

        public Tokenizer key(SecretKeySpec key) {
            this.key = key;
            return this;
        }

        public Tokenizer algParamSpec(AlgorithmParameterSpec algParamSpec) {
            this.algParamSpec = algParamSpec;
            return this;
        }

        public Tokenizer sessionId(SessionId sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public Tokenizer studentId(UserId studentId) {
            this.studentId = studentId;
            return this;
        }

        public Tokenizer requestedAt(OffsetDateTime requestedAt) {
            this.requestedAt = requestedAt;
            return this;
        }

        public Tokenizer expiresAt(OffsetDateTime expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        public Tokenizer expiresIn(Duration expiresIn) {
            this.expiresAt = requestedAt.plus(expiresIn);
            return this;
        }

        private Pass buildPass() {
            return new Pass(sessionId, studentId, requestedAt, expiresAt);
        }

        public ByteBuffer buildToken() {
            return buildPass().toToken(cipher, key, algParamSpec, charset);
        }

        public String buildTokenString() {
            return buildPass().toTokenString(cipher, key, algParamSpec, charset);
        }
    }

    public static final class Decoder {

        private Charset charset = DEFAULT_TEXT_CHARSET;

        private Cipher cipher;

        private SecretKeySpec key;

        private AlgorithmParameterSpec algParamSpec;

        private ByteBuffer tokenBuffer;

        public Decoder charset(Charset charset) {
            this.charset = charset;
            return this;
        }

        public Decoder cipher(Cipher cipher) {
            this.cipher = cipher;
            return this;
        }

        public Decoder key(SecretKeySpec key) {
            this.key = key;
            return this;
        }

        public Decoder algParamSpec(AlgorithmParameterSpec algParamSpec) {
            this.algParamSpec = algParamSpec;
            return this;
        }

        public Decoder token(ByteBuffer tokenBuffer) {
            this.tokenBuffer = tokenBuffer;
            return this;
        }

        public Decoder token(byte[] tokenBytes) {
            return token(ByteBuffer.wrap(tokenBytes));
        }

        public Decoder token(String tokenString, Charset charset) {
            return token(tokenString.getBytes(charset));
        }

        public Decoder token(String tokenString) {
            return token(tokenString.getBytes(charset));
        }

        public Pass decode() {
            return fromToken(cipher, key, algParamSpec, tokenBuffer, charset);
        }
    }
}
