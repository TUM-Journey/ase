package de.tum.ase.kleo.app.group.attendance.advertisement.handshake;

import java.time.Duration;

import de.tum.ase.kleo.app.client.GroupsApi;
import de.tum.ase.kleo.app.client.dto.PassDTO;

public class BackendHandshakeSupplier implements HandshakeServer.HandshakeSupplier {

    private static final Duration PASS_EXPIRE_IN = Duration.ofMinutes(2);

    private final GroupsApi groupsApi;

    public BackendHandshakeSupplier(GroupsApi groupsApi) {
        this.groupsApi = groupsApi;
    }

    @Override
    public String supply(String studentId, String groupIdOrCode, String sessionId) {
        final PassDTO passReqDto = new PassDTO()
                .studentId(studentId)
                .sessionId(sessionId)
                .expireIn((int) PASS_EXPIRE_IN.getSeconds());

        return groupsApi.generateSessionPass(groupIdOrCode, passReqDto).blockingFirst().getCode();
    }
}
