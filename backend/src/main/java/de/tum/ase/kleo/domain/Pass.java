package de.tum.ase.kleo.domain;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.time.Duration;
import java.time.OffsetDateTime;

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

    final static Charset DEFAULT_TEXT_CHARSET = Charset.forName("UTF-8");

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
}
