package tools.dynamia.commons;

import tools.dynamia.commons.logger.LoggingService;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Utility class for working with lambdas, consumers, and suppliers.
 * Provides methods to handle exceptions, memoization, chaining, composition, null safety, and string utilities.
 */
public class Lambdas {


    @FunctionalInterface
    public interface ThrowingConsumer<T> {
        void accept(T t) throws Exception;
    }

    @FunctionalInterface
    public interface ThrowingSupplier<T> {
        T get() throws Exception;
    }

    /**
     * Executes a Runnable, wrapping any checked exception in a RuntimeException.
     *
     * @param runnable the runnable to execute
     */
    public static void unchecked(Runnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a Consumer that wraps the given ThrowingConsumer, converting checked exceptions to RuntimeException.
     *
     * @param consumer the throwing consumer
     * @param <T>      the type of the input
     * @return a Consumer that does not throw checked exceptions
     */
    public static <T> Consumer<T> unchecked(ThrowingConsumer<T> consumer) {
        return t -> {
            try {
                consumer.accept(t);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    /**
     * Creates a Supplier that wraps the given ThrowingSupplier, converting checked exceptions to RuntimeException.
     *
     * @param supplier the throwing supplier
     * @param <T>      the type of the result
     * @return a Supplier that does not throw checked exceptions
     */
    public static <T> Supplier<T> unchecked(ThrowingSupplier<T> supplier) {
        return () -> {
            try {
                return supplier.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    /**
     * Returns a memoized Supplier that caches the result of the first call.
     * This is thread-safe.
     *
     * @param supplier the supplier to memoize
     * @param <T>      the type of the result
     * @return a memoized Supplier
     */
    public static <T> Supplier<T> memoize(Supplier<T> supplier) {
        return new Supplier<>() {
            private volatile T value;
            private volatile boolean initialized;

            @Override
            public T get() {
                if (!initialized) {
                    synchronized (this) {
                        if (!initialized) {
                            value = supplier.get();
                            initialized = true;
                        }
                    }
                }
                return value;
            }
        };
    }

    /**
     * Chains multiple Consumers into one that executes them in order.
     *
     * @param consumers the consumers to chain
     * @param <T>       the type of the input
     * @return a Consumer that executes all given consumers
     */
    @SafeVarargs
    public static <T> Consumer<T> chain(Consumer<T>... consumers) {
        return t -> {
            for (Consumer<T> consumer : consumers) {
                consumer.accept(t);
            }
        };
    }

    /**
     * Composes two Functions, applying the second after the first.
     *
     * @param first  the first function
     * @param second the second function
     * @param <T>    the input type
     * @param <U>    the intermediate type
     * @param <V>    the output type
     * @return the composed function
     */
    public static <T, U, V> Function<T, V> compose(Function<T, U> first, Function<U, V> second) {
        return t -> second.apply(first.apply(t));
    }

    /**
     * Creates a null-safe Consumer that only accepts non-null values.
     *
     * @param consumer the consumer to wrap
     * @param <T>      the type of the input
     * @return a Consumer that ignores null inputs
     */
    public static <T> Consumer<T> nullSafe(Consumer<T> consumer) {
        return t -> {
            if (t != null) {
                consumer.accept(t);
            }
        };
    }

    /**
     * Creates a null-safe Function that returns null if input is null, otherwise applies the function.
     *
     * @param function the function to wrap
     * @param <T>      the input type
     * @param <R>      the output type
     * @return a Function that handles null inputs
     */
    public static <T, R> Function<T, R> nullSafe(Function<T, R> function) {
        return t -> t == null ? null : function.apply(t);
    }

    /**
     * Creates a safe Supplier that returns null if an exception occurs.
     *
     * @param supplier the supplier to wrap
     * @param <T>      the type of the result
     * @return a Supplier that catches exceptions and returns null
     */
    public static <T> Supplier<T> safeCall(Supplier<T> supplier) {
        return () -> {
            try {
                return supplier.get();
            } catch (Exception e) {
                return null;
            }
        };
    }

    /**
     * Creates a safe Function that returns null if an exception occurs during application.
     *
     * @param function the function to wrap
     * @param <T>      the input type
     * @param <R>      the output type
     * @return a Function that catches exceptions and returns null
     */
    public static <T, R> Function<T, R> safeApply(Function<T, R> function) {
        return t -> {
            try {
                return function.apply(t);
            } catch (Exception e) {
                return null;
            }
        };
    }

    /**
     * Creates a Consumer for Strings that only accepts non-blank strings (not null and not empty after trim).
     *
     * @param consumer the consumer to wrap
     * @return a Consumer that ignores blank strings
     */
    public static Consumer<String> nonBlank(Consumer<String> consumer) {
        return s -> {
            if (s != null && !s.trim().isEmpty()) {
                consumer.accept(s);
            }
        };
    }

    /**
     * Returns a Function that trims the input string and returns it if non-blank, otherwise null.
     *
     * @return a Function for safe string handling
     */
    public static Function<String, String> nonBlankOrNull() {
        return s -> (s == null || s.trim().isEmpty()) ? null : s.trim();
    }

    /**
     * Executes the given consumer if the provided value is not null.
     *
     * @param value    the value to check
     * @param consumer the consumer to execute if value is not null
     * @param <T>      the type of the value
     */
    public static <T> void ifNotNull(T value, Consumer<T> consumer) {
        if (value != null) {
            consumer.accept(value);
        }
    }

    /**
     * Executes the given consumer if the provided string is not null or blank.
     *
     * @param value    the string to check
     * @param consumer the consumer to execute if string is not null or blank
     */
    public static void ifNotNull(String value, Consumer<String> consumer) {
        if (value != null && !value.isBlank()) {
            consumer.accept(value);
        }
    }


    /**
     * Executes the given consumer if the provided value is considered valid.
     * Validity checks:
     * - For String and CharSequence: not null and not blank
     * - For Collection and Map: not null and not empty
     * - For arrays: not null and length > 0
     * - For other types: not null
     *
     * @param value    the value to check
     * @param consumer the consumer to execute if value is valid
     * @param <T>      the type of the value
     */
    public static <T> void ifValid(T value, Consumer<T> consumer) {
        ifValidElse(value, consumer, null);
    }

    /**
     * Executes the given consumer if the provided value is considered valid.
     * Validity checks:
     * - For String and CharSequence: not null and not blank
     * - For Collection and Map: not null and not empty
     * - For arrays: not null and length > 0
     * - For other types: not null
     *
     * @param value    the value to check
     * @param consumer the consumer to execute if value is valid
     * @param <T>      the type of the value
     * @param fallback the consumer to execute if value is not valid
     */
    public static <T> void ifValidElse(T value, Consumer<T> consumer, Runnable fallback) {
        if (consumer == null) {
            return;
        }
        if (value == null) {
            return;
        }

        boolean valid = true;

        if (value instanceof String strValue) {
            valid = !strValue.isBlank();
        } else if (value instanceof CharSequence cs) {
            valid = !cs.toString().isBlank();
        } else if (value instanceof Collection<?> collectionValue) {
            valid = !collectionValue.isEmpty();
        } else if (value instanceof java.util.Map<?, ?> mapValue) {
            valid = !mapValue.isEmpty();
        } else if (value.getClass().isArray()) {
            valid = java.lang.reflect.Array.getLength(value) > 0;
        }

        if (valid) {
            consumer.accept(value);
        } else if (fallback != null) {
            fallback.run();
        }

    }


    /**
     * Executes the given consumer if the specified condition is true.
     *
     * @param condition the condition to evaluate
     * @param value     the value to pass to the consumer
     * @param consumer  the consumer to execute if condition is true
     * @param <T>       the type of the value
     */
    public static <T> void ifTrue(boolean condition, T value, Consumer<T> consumer) {
        if (condition) {
            consumer.accept(value);
        }
    }

    /**
     * Returns a Supplier that always supplies the given value.
     *
     * @param value the value to supply
     * @param <T>   the type of the value
     * @return a Supplier that returns the given value
     */
    public static <T> Supplier<T> supplierOf(T value) {
        return () -> value;
    }

    /**
     * Wraps a Supplier with try-catch, returning a fallback value on exception.
     *
     * @param supplier the supplier to wrap
     * @param fallback the fallback value
     * @param <T>      the type of the result
     * @return a Supplier with fallback
     */
    public static <T> Supplier<T> tryCatch(Supplier<T> supplier, T fallback) {
        return () -> {
            try {
                return supplier.get();
            } catch (Exception e) {
                return fallback;
            }
        };
    }

    /**
     * Executes a Runnable within a try-catch block, logging any exceptions.
     *
     * @param runnable
     */
    public static void tryCatch(Runnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            LoggingService.get(runnable.getClass()).error(e);
        }
    }
}