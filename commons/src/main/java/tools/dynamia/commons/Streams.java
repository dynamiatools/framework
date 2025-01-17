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
 * Streams utility class
 */
public class Streams {

    /**
     * Filter and collect to {@link List} in collection
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
     */
    public static <T, R> List<R> mapAndCollect(Collection<T> collection, Function<? super T, ? extends R> mapper) {

        if (collection == null) {
            return Collections.emptyList();
        }
        return collection.stream().map(mapper).collect(Collectors.toList());
    }

    /**
     * Filter and the map result to {@link List}
     */
    public static <T, R> List<R> mapAndCollectIf(Collection<T> collection, Predicate<T> predicate, Function<? super T, ? extends R> mapper) {
        if (collection == null) {
            return Collections.emptyList();
        }
        return collection.stream().filter(predicate).map(mapper).collect(Collectors.toList());
    }

    /**
     * Map collection to {@link BigDecimal} and collect to {@link List}
     */
    public static <T> List<BigDecimal> mapToBigDecimal(Collection<T> collection, Function<T, BigDecimal> toBigDecimalFunction) {
        if (collection == null) {
            return Collections.emptyList();
        }
        return collection.stream().map(toBigDecimalFunction).collect(Collectors.toList());
    }

    /**
     *
     */
    public static <T> List<BigDecimal> mapToBigDecimalIf(Collection<T> collection, Predicate<T> predicate, Function<T, BigDecimal> toBigDecimalFunction) {
        if (collection == null) {
            return Collections.emptyList();
        }
        return collection.stream().filter(predicate)
                .map(toBigDecimalFunction).collect(Collectors.toList());
    }

    /**
     * Map and collect result to {@link List}
     *
     * @param elements
     * @param mapper
     * @param <T>
     * @param <R>
     * @return
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
