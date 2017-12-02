package de.tum.ase.kleo.application.api.mapping;

import de.tum.ase.kleo.application.api.dto.AttendanceDTO;
import de.tum.ase.kleo.application.api.dto.UserDTO;
import de.tum.ase.kleo.domain.Pass;
import de.tum.ase.kleo.domain.User;
import de.tum.ase.kleo.domain.UserRepository;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SessionAttendanceDtoMapper implements Mapper<Pass, AttendanceDTO> {

    private final UserRepository userRepository;
    private final Mapper<User, UserDTO> userDtoMapper;

    @Autowired
    public SessionAttendanceDtoMapper(UserRepository userRepository, Mapper<User, UserDTO> userDtoMapper) {
        this.userRepository = userRepository;
        this.userDtoMapper = userDtoMapper;
    }

    @Override
    public AttendanceDTO map(Pass source) {
        if (source == null)
            return null;

        if (!source.isUsed())
            throw new MappingException("The Pass given has not being used. Cannot extract Attendance DTO");

        val user = userRepository.findByPassesContains(source)
                .orElseThrow(() -> new MappingException("Failed to find the holder (user) of pass given"));

        return new AttendanceDTO()
                .passId(source.id())
                .user(userDtoMapper.map(user))
                .timestamp(source.usedDateTime().toOffsetDateTime());
    }
}
