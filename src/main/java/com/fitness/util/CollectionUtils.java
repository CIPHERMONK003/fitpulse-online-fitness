package com.fitness.util;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Generic Collection Utilities demonstrating advanced Collections & Generics.
 * Includes:
 * - Generic filtering and transformations
 * - Stream API manipulations
 * - Generic aggregation and custom comparator sorters
 */
public final class CollectionUtils {

    private CollectionUtils() {}

    /**
     * Generic filter method returning a new ArrayList of elements matching predicate.
     */
    public static <T> List<T> filter(Collection<T> source, Predicate<T> predicate) {
        if (source == null) return Collections.emptyList();
        return source.stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

    /**
     * Generic map/transformation method.
     */
    public static <T, R> List<R> map(Collection<T> source, Function<T, R> mapper) {
        if (source == null) return Collections.emptyList();
        return source.stream()
                .map(mapper)
                .collect(Collectors.toList());
    }

    /**
     * Generic sorting method using a Comparator.
     */
    public static <T> List<T> sorted(Collection<T> source, Comparator<? super T> comparator) {
        if (source == null) return Collections.emptyList();
        return source.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    /**
     * Generic method to find the first element matching a predicate.
     */
    public static <T> Optional<T> findFirst(Collection<T> source, Predicate<T> predicate) {
        if (source == null) return Optional.empty();
        return source.stream().filter(predicate).findFirst();
    }

    /**
     * Groups items by a key extractor function into a Map<K, List<V>>.
     */
    public static <K, V> Map<K, List<V>> groupBy(Collection<V> source, Function<V, K> classifier) {
        if (source == null) return Collections.emptyMap();
        return source.stream().collect(Collectors.groupingBy(classifier));
    }

    /**
     * Converts a collection to an unmodifiable Set.
     */
    public static <T> Set<T> toSet(Collection<T> source) {
        if (source == null) return Collections.emptySet();
        return Collections.unmodifiableSet(new HashSet<>(source));
    }
}
