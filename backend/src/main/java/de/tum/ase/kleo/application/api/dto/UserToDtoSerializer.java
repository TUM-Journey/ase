package de.tum.ase.kleo.application.api.dto;

import de.tum.ase.kleo.domain.User;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

@Component
public class UserToDtoSerializer {

    public UserDTO toDto(User source) {
        if (source == null)
            return null;

        return new UserDTO()
                .id(source.id().toString())
                .name(source.name())
                .email(source.email())
                .studentId(source.studentId());
    }

    public List<UserDTO> toDto(Iterable<User> sources) {
        return stream(sources.spliterator(), false).map(this::toDto).collect(toList());
    }
}