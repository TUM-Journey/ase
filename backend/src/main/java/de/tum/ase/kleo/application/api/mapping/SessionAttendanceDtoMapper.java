package de.tum.ase.kleo.application.api.mapping;

import de.tum.ase.kleo.application.api.dto.AttendanceDTO;
import de.tum.ase.kleo.domain.User;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

@Component
public class SessionAttendanceDtoMapper implements Mapper<User, List<AttendanceDTO>> {

    private final UserToDtoMapper userToDtoMapper;
    private final SessionToDtoMapper sessionToDtoMapper;

    @Autowired
    public SessionAttendanceDtoMapper(UserToDtoMapper userToDtoMapper, SessionToDtoMapper sessionToDtoMapper) {
        this.userToDtoMapper = userToDtoMapper;
        this.sessionToDtoMapper = sessionToDtoMapper;
    }


    @Override
    public List<AttendanceDTO> map(User source) {
        if (source == null)
            return null;

        val usedPasses = source.usedPasses();
        if (usedPasses.isEmpty())
            return emptyList();

        return usedPasses.stream()
                .map(pass -> new AttendanceDTO()
                        .passId(pass.id())
                        .session(sessionToDtoMapper.map(pass.session()))
                        .timestamp(pass.usedDateTime().toOffsetDateTime())
                        .user(userToDtoMapper.map(source)))
                .collect(toList());
    }
}
