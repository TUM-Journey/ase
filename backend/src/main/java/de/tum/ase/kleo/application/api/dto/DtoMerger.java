package de.tum.ase.kleo.application.api.dto;

/**
 * {@code Merger} implementations merges properties from
 * source object to destination object
 *
 * @param <DTO> source type
 * @param <DO> destination type
 */
public interface DtoMerger<DTO, DO> {

    void merge(DTO source, DO dest);
}
