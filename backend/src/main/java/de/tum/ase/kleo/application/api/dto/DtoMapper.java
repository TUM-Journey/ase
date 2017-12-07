package de.tum.ase.kleo.application.api.dto;

public interface DtoMapper<DO, DTO> extends DtoSerializer<DO, DTO>, DtoDeserializer<DTO, DO> {
}
