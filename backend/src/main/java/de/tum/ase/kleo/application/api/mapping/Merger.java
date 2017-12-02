package de.tum.ase.kleo.application.api.mapping;

import java.util.List;

/**
 * {@code Merger} implementations merges properties from
 * source object to destination object
 *
 * @param <S> source type
 * @param <D> destination type
 */
public interface Merger<S, D> {

    void merge(S source, D dest);

    default void merge(List<S> sources, List<D> dests) {
        if (sources == null)
            return;

        if (sources.size() != dests.size())
            throw new IllegalArgumentException("Merge sources.size() != destinations.size()");

        for (int i = 0; i < sources.size(); i++) {
            merge(sources.get(i), dests.get(i));
        }
    }
}
