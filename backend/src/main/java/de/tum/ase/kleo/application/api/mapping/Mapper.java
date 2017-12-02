package de.tum.ase.kleo.application.api.mapping;

import java.util.Collection;

import static java.util.stream.Collectors.toList;

/**
 * {@code Mapper} implementations maps properties from
 * source object to destination object
 *
 * @param <S> source type
 * @param <D> destination type
 */
public interface Mapper<S, D> {

    D map(S source);

    default Collection<D> map(Collection<S> sources) {
        if (sources == null)
            return null;

        return sources.stream().map(this::map).collect(toList());
    }
}
