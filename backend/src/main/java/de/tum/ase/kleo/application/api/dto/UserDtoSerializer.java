package de.tum.ase.kleo.application.api.dto;

import de.tum.ase.kleo.domain.User;
import org.springframework.stereotype.Component;

@Component
public class UserDtoSerializer implements DtoSerializer<User, UserDTO> {

    @Override
    public UserDTO toDto(User source) {
        if (source == null)
            return null;

        return new UserDTO()
                .id(source.id().toString())
                .name(source.name())
                .email(source.email())
                .studentId(source.studentId());
    }
}
