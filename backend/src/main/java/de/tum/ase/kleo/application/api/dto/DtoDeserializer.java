package de.tum.ase.kleo.application.api.dto;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

public interface DtoDeserializer<DTO, DO> {

    DO fromDto(DTO dto);

    default List<DO> fromDto(Iterable<DTO> dtos) {
        return stream(dtos.spliterator(), false).map(this::fromDto).collect(toList());
    }
}