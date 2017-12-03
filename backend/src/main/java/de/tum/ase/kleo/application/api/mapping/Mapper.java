package de.tum.ase.kleo.application.api.mapping;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

/**
 * {@code Mapper} implementations maps properties from
 * source object to destination object
 *
 * @param <S> source type
 * @param <D> destination type
 */
public interface Mapper<S, D> {

    D map(S source);

    default List<D> map(Iterable<S> sources) {
        if (sources == null)
            return null;

        return stream(sources.spliterator(), false).map(this::map).collect(toList());
    }
}
