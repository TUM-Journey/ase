package de.tum.ase.kleo.application.api.dto;

import de.tum.ase.kleo.domain.User;
import de.tum.ase.kleo.domain.UserRole;

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
                .studentId(source.studentId())
                .roles(toUserRoleDto(source.userRoles()));
    }

    public List<UserDTO> toDto(Iterable<User> sources) {
        return stream(sources.spliterator(), false).map(this::toDto).collect(toList());
    }

    private List<UserDTO.RolesEnum> toUserRoleDto(List<UserRole> userRoles) {
        return userRoles.stream()
                .map(UserRole::toString)
                .map(UserDTO.RolesEnum::fromValue).collect(toList());
    }
}