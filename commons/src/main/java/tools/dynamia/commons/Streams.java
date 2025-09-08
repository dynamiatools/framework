package tools.dynamia.commons;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility class for working with Java Streams and collections.
 * <p>
 * Provides static helper methods for filtering, mapping, and collecting elements from collections and arrays.
 * Includes methods for converting to {@link List}, {@link Set}, and for mapping to {@link BigDecimal}.
 * <p>
 * All methods are stateless and thread-safe.
 *
 * @author Mario A. Serrano Leones
 */
public class Streams {

    /**
     * Filters a collection using the given predicate and collects the results into a {@link List}.
     *
     * @param collection the input collection
     * @param predicate the filter predicate
     * @param <T> the element type
     * @return a list of filtered elements
     */
    public static <T> List<T> collectIf(Collection<T> collection, Predicate<T> predicate) {
        if (collection == null) {
            return Collections.emptyList();
        }

        return collection.stream().filter(predicate)
                .collect(Collectors.toList());
    }


    /**
     * Filters a collection using the given predicate and collects the results into a {@link Set}.
     *
     * @param collection the input collection
     * @param predicate the filter predicate
     * @param <T> the element type
     * @return a set of filtered elements
     */
    public static <T> Set<T> collectSetIf(Collection<T> collection, Predicate<T> predicate) {
        if (collection == null) {
            return Collections.emptySet();
        }

        return collection.stream().filter(predicate)
                .collect(Collectors.toSet());
    }

    /**
     * Maps a collection using the given mapper function and collects the results into a {@link List}.
     *
     * @param collection the input collection
     * @param mapper the mapping function
     * @param <T> the input element type
     * @param <R> the result type
     * @return a list of mapped elements
     */
    public static <T, R> List<R> mapAndCollect(Collection<T> collection, Function<? super T, ? extends R> mapper) {

        if (collection == null) {
            return Collections.emptyList();
        }
        return collection.stream().map(mapper).collect(Collectors.toList());
    }

    /**
     * Filters a collection using the given predicate, maps the results, and collects them into a {@link List}.
     *
     * @param collection the input collection
     * @param predicate the filter predicate
     * @param mapper the mapping function
     * @param <T> the input element type
     * @param <R> the result type
     * @return a list of filtered and mapped elements
     */
    public static <T, R> List<R> mapAndCollectIf(Collection<T> collection, Predicate<T> predicate, Function<? super T, ? extends R> mapper) {
        if (collection == null) {
            return Collections.emptyList();
        }
        return collection.stream().filter(predicate).map(mapper).collect(Collectors.toList());
    }

    /**
     * Maps a collection to {@link BigDecimal} using the given function and collects the results into a {@link List}.
     *
     * @param collection the input collection
     * @param toBigDecimalFunction the mapping function to BigDecimal
     * @param <T> the input element type
     * @return a list of BigDecimal values
     */
    public static <T> List<BigDecimal> mapToBigDecimal(Collection<T> collection, Function<T, BigDecimal> toBigDecimalFunction) {
        if (collection == null) {
            return Collections.emptyList();
        }
        return collection.stream().map(toBigDecimalFunction).collect(Collectors.toList());
    }

    /**
     * Filters a collection using the given predicate, maps to {@link BigDecimal}, and collects the results into a {@link List}.
     *
     * @param collection the input collection
     * @param predicate the filter predicate
     * @param toBigDecimalFunction the mapping function to BigDecimal
     * @param <T> the input element type
     * @return a list of filtered BigDecimal values
     */
    public static <T> List<BigDecimal> mapToBigDecimalIf(Collection<T> collection, Predicate<T> predicate, Function<T, BigDecimal> toBigDecimalFunction) {
        if (collection == null) {
            return Collections.emptyList();
        }
        return collection.stream().filter(predicate)
                .map(toBigDecimalFunction).collect(Collectors.toList());
    }

    /**
     * Maps an array using the given mapper function and collects the results into a {@link List}.
     *
     * @param elements the input array
     * @param mapper the mapping function
     * @param <T> the input element type
     * @param <R> the result type
     * @return a list of mapped elements
     */
    public static <T, R> List<R> mapAndCollect(T[] elements, Function<T, R> mapper) {
        return Stream.of(elements).map(mapper).toList();
    }


    /**
     * Sum a collections of bigdecimals
     */
    public BigDecimal sum(Collection<BigDecimal> numbers) {
        if (numbers == null) {
            return BigDecimal.ZERO;
        }

        return numbers.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    /**
     * Map collection to {@link BigDecimal} and reduce sum
     */
    public static <T> BigDecimal mapAndSum(Collection<T> collection, Function<T, BigDecimal> toBigDecimalFunction) {
        if (collection == null) {
            return BigDecimal.ZERO;
        }
        return collection.stream().map(toBigDecimalFunction).reduce(BigDecimal.ZERO, BigDecimal::add);

    }

    /**
     * Helper to build arrays
     */
    @SafeVarargs
    public static <T> T[] toArray(T... values) {
        return values;
    }

    public static <T> void forEachIf(Collection<T> collection, Predicate<T> predicate, Consumer<T> action) {
        collection.stream().filter(predicate).forEach(action);
    }

    /**
     * Filter collection and find the first element
     */
    public static <T> Optional<T> findFirstIf(Collection<T> collection, Predicate<T> predicate) {
        if (collection == null) {
            return Optional.empty();
        }
        return collection.stream().filter(predicate)
                .findFirst();
    }

    /**
     * Filter collection and find the first element or null
     */
    public static <T> T findFirstElseNull(Collection<T> collection, Predicate<T> predicate) {
        if (collection == null) {
            return null;
        }

        return collection.stream().filter(predicate)
                .findFirst()
                .orElse(null);
    }
}
