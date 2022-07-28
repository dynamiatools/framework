package tools.dynamia.commons;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Streams utility class
 */
public class Streams {

    /**
     * Filter and collect to {@link List} in collection
     *
     * @param collection
     * @param predicate
     * @param <T>
     * @return
     */
    public static <T> List<T> collectIf(Collection<T> collection, Predicate<T> predicate) {
        if (collection == null) {
            return Collections.emptyList();
        }

        return collection.stream().filter(predicate)
                .collect(Collectors.toList());
    }


    /**
     * Filter and collect to {@link Set} in collection
     *
     * @param collection
     * @param predicate
     * @param <T>
     * @return
     */
    public static <T> Set<T> collectSetIf(Collection<T> collection, Predicate<T> predicate) {
        if (collection == null) {
            return Collections.emptySet();
        }

        return collection.stream().filter(predicate)
                .collect(Collectors.toSet());
    }

    /**
     * Map and collect result to {@link List}
     *
     * @param collection
     * @param mapper
     * @param <T>
     * @param <R>
     * @return
     */
    public static <T, R> List<R> mapAndCollect(Collection<T> collection, Function<? super T, ? extends R> mapper) {

        if (collection == null) {
            return Collections.emptyList();
        }
        return collection.stream().map(mapper).collect(Collectors.toList());
    }

    /**
     * Filter and the map result to {@link List}
     *
     * @param collection
     * @param predicate
     * @param mapper
     * @param <T>
     * @param <R>
     * @return
     */
    public static <T, R> List<R> mapAndCollectIf(Collection<T> collection, Predicate<T> predicate, Function<? super T, ? extends R> mapper) {
        if (collection == null) {
            return Collections.emptyList();
        }
        return collection.stream().filter(predicate).map(mapper).collect(Collectors.toList());
    }

    /**
     * Map collection to {@link BigDecimal} and collect to {@link List}
     *
     * @param collection
     * @param toBigDecimalFunction
     * @param <T>
     * @return
     */
    public static <T> List<BigDecimal> mapToBigDecimal(Collection<T> collection, Function<T, BigDecimal> toBigDecimalFunction) {
        if (collection == null) {
            return Collections.emptyList();
        }
        return collection.stream().map(toBigDecimalFunction).collect(Collectors.toList());
    }

    /**
     * @param collection
     * @param predicate
     * @param toBigDecimalFunction
     * @param <T>
     * @return
     */
    public static <T> List<BigDecimal> mapToBigDecimalIf(Collection<T> collection, Predicate<T> predicate, Function<T, BigDecimal> toBigDecimalFunction) {
        if (collection == null) {
            return Collections.emptyList();
        }
        return collection.stream().filter(predicate)
                .map(toBigDecimalFunction).collect(Collectors.toList());
    }


    /**
     * Sum a collections of bigdecimals
     *
     * @param numbers
     * @return
     */
    public BigDecimal sum(Collection<BigDecimal> numbers) {
        if (numbers == null) {
            return BigDecimal.ZERO;
        }

        return numbers.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    /**
     * Map collection to {@link BigDecimal} and reduce sum
     *
     * @param collection
     * @param toBigDecimalFunction
     * @param <T>
     * @return
     */
    public static <T> BigDecimal mapAndSum(Collection<T> collection, Function<T, BigDecimal> toBigDecimalFunction) {
        if (collection == null) {
            return BigDecimal.ZERO;
        }
        return collection.stream().map(toBigDecimalFunction).reduce(BigDecimal.ZERO, BigDecimal::add);

    }

    /**
     * Helper to build arrays
     *
     * @param values
     * @param <T>
     * @return
     */
    public static <T> T[] toArray(T... values) {
        return values;
    }

    public static <T> void forEachIf(Collection<T> collection, Predicate<T> predicate, Consumer<T> action) {
        collection.stream().filter(predicate).forEach(action);
    }

    /**
     * Filter collection and find the first element
     *
     * @param collection
     * @param predicate
     * @param <T>
     * @return
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
     *
     * @param collection
     * @param predicate
     * @param <T>
     * @return
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
