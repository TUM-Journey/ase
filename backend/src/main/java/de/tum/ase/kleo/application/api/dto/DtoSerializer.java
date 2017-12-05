package de.tum.ase.kleo.application.api.dto;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

public interface DtoSerializer<DO, DTO> {

    DTO toDto(DO source);

    default List<DTO> toDto(Iterable<DO> sources) {
        return stream(sources.spliterator(), false).map(this::toDto).collect(toList());
    }
}
