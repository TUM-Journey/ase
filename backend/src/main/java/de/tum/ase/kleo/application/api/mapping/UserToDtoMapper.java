package de.tum.ase.kleo.application.api.mapping;

import de.tum.ase.kleo.application.api.dto.UserDTO;
import de.tum.ase.kleo.domain.User;
import org.springframework.stereotype.Component;

@Component
public class UserToDtoMapper implements Mapper<User, UserDTO> {

    @Override
    public UserDTO map(User source) {
        if (source == null)
            return null;

        return new UserDTO()
                .id(source.id())
                .email(source.email())
                .studentId(source.studentId())
                .name(source.name());
    }
}
