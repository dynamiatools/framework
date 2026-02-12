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
     * Useful for executing code with checked exceptions in contexts that don't allow them.
     *
     * <p>Example:
     * <pre>{@code
     * Lambdas.unchecked(() -> {
     *     Thread.sleep(1000); // throws InterruptedException
     *     Files.delete(path); // throws IOException
     * });
     * }</pre>
     *
     * @param runnable the runnable to execute
     * @throws RuntimeException if the runnable throws any exception
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
     * Useful for using lambdas with checked exceptions in Stream APIs.
     *
     * <p>Example:
     * <pre>{@code
     * List<String> files = Arrays.asList("file1.txt", "file2.txt");
     * files.forEach(Lambdas.unchecked(file -> {
     *     String content = Files.readString(Path.of(file));
     *     System.out.println(content);
     * }));
     * }</pre>
     *
     * @param consumer the throwing consumer
     * @param <T>      the type of the input
     * @return a Consumer that does not throw checked exceptions
     * @throws RuntimeException if the consumer throws any exception
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
     * Ideal for lazy initialization or factory methods that may throw checked exceptions.
     *
     * <p>Example:
     * <pre>{@code
     * Supplier<String> contentSupplier = Lambdas.unchecked(() ->
     *     Files.readString(Path.of("config.txt"))
     * );
     * String content = contentSupplier.get();
     * }</pre>
     *
     * @param supplier the throwing supplier
     * @param <T>      the type of the result
     * @return a Supplier that does not throw checked exceptions
     * @throws RuntimeException if the supplier throws any exception
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
     * Subsequent calls return the cached value without re-executing the supplier.
     * This is thread-safe using double-checked locking.
     *
     * <p>Example:
     * <pre>{@code
     * Supplier<ExpensiveObject> cached = Lambdas.memoize(() -> {
     *     System.out.println("Computing...");
     *     return new ExpensiveObject();
     * });
     *
     * cached.get(); // prints "Computing..." and returns object
     * cached.get(); // returns same object without printing
     * }</pre>
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
     * Useful for composing multiple operations on the same value.
     *
     * <p>Example:
     * <pre>{@code
     * Consumer<String> validate = s -> System.out.println("Validating: " + s);
     * Consumer<String> save = s -> System.out.println("Saving: " + s);
     * Consumer<String> notify = s -> System.out.println("Notifying: " + s);
     *
     * Consumer<String> process = Lambdas.chain(validate, save, notify);
     * process.accept("data"); // executes all three consumers
     * }</pre>
     *
     * @param consumers the consumers to chain
     * @param <T>       the type of the input
     * @return a Consumer that executes all given consumers
     */
    @SafeVarargs
    public static <T> Consumer<T> chain(Consumer<T>... consumers) {
        return t -> {
            for (Consumer<T> consumer : consumers) {
                if (consumer != null) {
                    consumer.accept(t);
                }
            }
        };
    }

    /**
     * Composes two Functions, applying the second after the first.
     * Equivalent to f.andThen(g) or g.compose(f).
     *
     * <p>Example:
     * <pre>{@code
     * Function<String, Integer> parseLength = s -> s.length();
     * Function<Integer, String> format = i -> "Length: " + i;
     *
     * Function<String, String> combined = Lambdas.compose(parseLength, format);
     * String result = combined.apply("Hello"); // "Length: 5"
     * }</pre>
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
     * If the value is null, the consumer is not executed.
     *
     * <p>Example:
     * <pre>{@code
     * Consumer<String> print = Lambdas.nullSafe(s -> System.out.println(s.toUpperCase()));
     * print.accept("hello"); // prints "HELLO"
     * print.accept(null);    // does nothing (no NullPointerException)
     * }</pre>
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
     * <p>Example:
     * <pre>{@code
     * Function<String, Integer> length = Lambdas.nullSafe(String::length);
     * Integer result1 = length.apply("hello"); // returns 5
     * Integer result2 = length.apply(null);    // returns null
     * }</pre>
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
     * Useful for optional operations that may fail without breaking the flow.
     *
     * <p>Example:
     * <pre>{@code
     * Supplier<String> config = Lambdas.safeCall(() ->
     *     Files.readString(Path.of("config.txt"))
     * );
     * String value = config.get(); // returns null if file doesn't exist
     * }</pre>
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
     * Ideal for transformations that may fail on invalid input.
     *
     * <p>Example:
     * <pre>{@code
     * Function<String, Integer> parse = Lambdas.safeApply(Integer::parseInt);
     * Integer num1 = parse.apply("123");   // returns 123
     * Integer num2 = parse.apply("invalid"); // returns null instead of throwing
     * }</pre>
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
     * Useful for processing user input or configuration values.
     *
     * <p>Example:
     * <pre>{@code
     * Consumer<String> process = Lambdas.nonBlank(s -> System.out.println("Processing: " + s));
     * process.accept("hello");  // prints "Processing: hello"
     * process.accept("");       // does nothing
     * process.accept("   ");    // does nothing
     * process.accept(null);     // does nothing
     * }</pre>
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
     * Useful for sanitizing and validating string inputs in a functional pipeline.
     *
     * <p>Example:
     * <pre>{@code
     * Function<String, String> sanitize = Lambdas.nonBlankOrNull();
     * String result1 = sanitize.apply("  hello  "); // returns "hello"
     * String result2 = sanitize.apply("");          // returns null
     * String result3 = sanitize.apply("   ");       // returns null
     * String result4 = sanitize.apply(null);        // returns null
     * }</pre>
     *
     * @return a Function for safe string handling
     */
    public static Function<String, String> nonBlankOrNull() {
        return s -> (s == null || s.trim().isEmpty()) ? null : s.trim();
    }

    /**
     * Executes the given consumer if the provided value is not null.
     * Provides a cleaner alternative to manual null checks.
     *
     * <p>Example:
     * <pre>{@code
     * User user = getUser();
     * Lambdas.ifNotNull(user, u -> System.out.println("User: " + u.getName()));
     *
     * // Instead of:
     * // if (user != null) {
     * //     System.out.println("User: " + user.getName());
     * // }
     * }</pre>
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
     * Combines null checking with blank string validation.
     *
     * <p>Example:
     * <pre>{@code
     * String email = getEmail();
     * Lambdas.ifNotNull(email, e -> sendNotification(e));
     *
     * Lambdas.ifNotNull("", s -> System.out.println(s));    // does nothing
     * Lambdas.ifNotNull("  ", s -> System.out.println(s));  // does nothing
     * Lambdas.ifNotNull("hello", s -> System.out.println(s)); // prints "hello"
     * }</pre>
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
     * <p>Example:
     * <pre>{@code
     * List<String> items = getItems();
     * Lambdas.ifValid(items, list -> processItems(list));
     *
     * String name = getName();
     * Lambdas.ifValid(name, n -> System.out.println("Name: " + n));
     *
     * // Does nothing with empty collections or blank strings
     * Lambdas.ifValid(Collections.emptyList(), list -> process(list)); // skipped
     * Lambdas.ifValid("", str -> process(str));                        // skipped
     * }</pre>
     *
     * @param value    the value to check
     * @param consumer the consumer to execute if value is valid
     * @param <T>      the type of the value
     */
    public static <T> void ifValid(T value, Consumer<T> consumer) {
        ifValidElse(value, consumer, null);
    }

    /**
     * Executes the given consumer if the provided value is considered valid, otherwise executes the fallback.
     * Validity checks:
     * - For String and CharSequence: not null and not blank
     * - For Collection and Map: not null and not empty
     * - For arrays: not null and length > 0
     * - For other types: not null
     *
     * <p>Example:
     * <pre>{@code
     * List<String> items = getItems();
     * Lambdas.ifValidElse(
     *     items,
     *     list -> System.out.println("Processing " + list.size() + " items"),
     *     () -> System.out.println("No items to process")
     * );
     *
     * String config = getConfig();
     * Lambdas.ifValidElse(
     *     config,
     *     c -> applyConfig(c),
     *     () -> useDefaultConfig()
     * );
     * }</pre>
     *
     * @param value    the value to check
     * @param consumer the consumer to execute if value is valid
     * @param fallback the runnable to execute if value is not valid
     * @param <T>      the type of the value
     */
    public static <T> void ifValidElse(T value, Consumer<T> consumer, Runnable fallback) {
        if (consumer == null) {
            return;
        }
        if (value == null) {
            if (fallback != null) {
                fallback.run();
            }
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
     * Useful for conditional operations with a value in a functional style.
     *
     * <p>Example:
     * <pre>{@code
     * User user = getUser();
     * boolean isAdmin = user.hasRole("ADMIN");
     *
     * Lambdas.ifTrue(isAdmin, user, u -> grantAdminAccess(u));
     *
     * // With inline condition
     * Lambdas.ifTrue(score > 100, score, s -> System.out.println("High score: " + s));
     * }</pre>
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
     * Useful for converting a constant value into a Supplier for API compatibility.
     *
     * <p>Example:
     * <pre>{@code
     * Supplier<String> defaultName = Lambdas.supplierOf("Guest");
     * System.out.println(defaultName.get()); // prints "Guest"
     *
     * // Useful in method parameters that expect Supplier
     * void processWithDefault(Supplier<String> valueSupplier) {
     *     String value = valueSupplier.get();
     *     // process value
     * }
     *
     * processWithDefault(Lambdas.supplierOf("Default Value"));
     * }</pre>
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
     * Ideal for operations that may fail but should have a default value.
     *
     * <p>Example:
     * <pre>{@code
     * Supplier<Integer> port = Lambdas.tryCatch(
     *     () -> Integer.parseInt(System.getProperty("server.port")),
     *     8080  // fallback port
     * );
     * int serverPort = port.get(); // returns 8080 if property is missing or invalid
     *
     * // Reading configuration with fallback
     * Supplier<String> configValue = Lambdas.tryCatch(
     *     () -> Files.readString(Path.of("config.txt")),
     *     "default-config"
     * );
     * }</pre>
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
     * Useful for fire-and-forget operations where exceptions should be logged but not propagated.
     *
     * <p>Example:
     * <pre>{@code
     * // Execute cleanup operations without failing the main flow
     * Lambdas.tryCatch(() -> {
     *     Files.delete(tempFile);
     *     cache.clear();
     * });
     *
     * // Send notification without breaking the process
     * Lambdas.tryCatch(() -> notificationService.send(message));
     *
     * // Multiple operations with safe execution
     * Lambdas.tryCatch(() -> {
     *     logActivity(user);
     *     updateStatistics();
     * });
     * }</pre>
     *
     * @param runnable the runnable to execute
     */
    public static void tryCatch(Runnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            LoggingService.get(runnable.getClass()).error(e);
        }
    }

    /**
     * Executes the given consumer if the specified condition is false.
     * Opposite of ifTrue, useful for negative condition checks.
     *
     * <p>Example:
     * <pre>{@code
     * User user = getUser();
     * boolean isActive = user.isActive();
     *
     * Lambdas.ifFalse(isActive, user, u -> sendReactivationEmail(u));
     *
     * // With inline condition
     * Lambdas.ifFalse(balance >= 0, account, a -> System.out.println("Negative balance: " + a));
     * }</pre>
     *
     * @param condition the condition to evaluate
     * @param value     the value to pass to the consumer
     * @param consumer  the consumer to execute if condition is false
     * @param <T>       the type of the value
     */
    public static <T> void ifFalse(boolean condition, T value, Consumer<T> consumer) {
        if (!condition) {
            consumer.accept(value);
        }
    }

    /**
     * Creates a Function that applies a side effect (consumer) and returns the input unchanged.
     * Useful for debugging or logging in functional pipelines.
     *
     * <p>Example:
     * <pre>{@code
     * List<String> names = Arrays.asList("Alice", "Bob", "Charlie");
     * List<String> upperNames = names.stream()
     *     .map(Lambdas.peek(name -> System.out.println("Processing: " + name)))
     *     .map(String::toUpperCase)
     *     .collect(Collectors.toList());
     * // Prints each name before converting to uppercase
     * }</pre>
     *
     * @param consumer the consumer to apply as a side effect
     * @param <T>      the type of the value
     * @return a Function that applies the consumer and returns the input
     */
    public static <T> Function<T, T> peek(Consumer<T> consumer) {
        return t -> {
            consumer.accept(t);
            return t;
        };
    }

    /**
     * Executes a consumer on a value and returns the value unchanged.
     * Alias for peek, useful for method chaining and fluent APIs.
     *
     * <p>Example:
     * <pre>{@code
     * User user = new User("John");
     * User result = Lambdas.tap(user, u -> u.setEmail("john@example.com"));
     * // result is the same user object with email set
     *
     * // Useful in fluent chains
     * return Lambdas.tap(buildReport(), r -> System.out.println("Report built: " + r.getId()));
     * }</pre>
     *
     * @param value    the value to process
     * @param consumer the consumer to execute
     * @param <T>      the type of the value
     * @return the original value unchanged
     */
    public static <T> T tap(T value, Consumer<T> consumer) {
        consumer.accept(value);
        return value;
    }

    /**
     * Creates a Supplier that retries the operation a specified number of times on failure.
     * Useful for operations that may fail temporarily (network calls, database operations).
     *
     * <p>Example:
     * <pre>{@code
     * Supplier<String> apiCall = Lambdas.retry(() -> {
     *     return httpClient.get("https://api.example.com/data");
     * }, 3); // retry up to 3 times
     *
     * String data = apiCall.get(); // will retry on failure
     *
     * // Database operation with retry
     * Supplier<User> findUser = Lambdas.retry(() -> database.findUser(id), 5);
     * }</pre>
     *
     * @param supplier   the supplier to retry
     * @param maxRetries the maximum number of retry attempts
     * @param <T>        the type of the result
     * @return a Supplier that retries on failure
     * @throws RuntimeException if all retry attempts fail
     */
    public static <T> Supplier<T> retry(Supplier<T> supplier, int maxRetries) {
        return () -> {
            Exception lastException = null;
            for (int i = 0; i <= maxRetries; i++) {
                try {
                    return supplier.get();
                } catch (Exception e) {
                    lastException = e;
                    if (i < maxRetries) {
                        try {
                            Thread.sleep(100L * (i + 1)); // exponential backoff
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw new RuntimeException(ie);
                        }
                    }
                }
            }
            throw new RuntimeException("Failed after " + maxRetries + " retries", lastException);
        };
    }

    /**
     * Creates a lazy-initialized Supplier that computes the value only once, on first access.
     * Similar to memoize but emphasizes lazy evaluation semantics.
     * Thread-safe implementation.
     *
     * <p>Example:
     * <pre>{@code
     * Supplier<Database> db = Lambdas.lazy(() -> {
     *     System.out.println("Connecting to database...");
     *     return new Database();
     * });
     *
     * // Database connection not created yet
     * System.out.println("Before access");
     * Database database = db.get(); // prints "Connecting to database..."
     * Database same = db.get(); // returns cached instance, no print
     * }</pre>
     *
     * @param supplier the supplier for lazy initialization
     * @param <T>      the type of the result
     * @return a lazy Supplier
     */
    public static <T> Supplier<T> lazy(Supplier<T> supplier) {
        return memoize(supplier);
    }

    /**
     * Creates a Consumer that executes only if the predicate is satisfied.
     * Combines filtering and consuming in a single operation.
     *
     * <p>Example:
     * <pre>{@code
     * Consumer<User> processActiveUsers = Lambdas.filter(
     *     user -> user.isActive(),
     *     user -> processUser(user)
     * );
     *
     * users.forEach(processActiveUsers); // only active users are processed
     *
     * // With method references
     * Consumer<String> printLong = Lambdas.filter(
     *     s -> s.length() > 5,
     *     System.out::println
     * );
     * }</pre>
     *
     * @param predicate the predicate to test
     * @param consumer  the consumer to execute if predicate is true
     * @param <T>       the type of the input
     * @return a Consumer that filters before executing
     */
    public static <T> Consumer<T> filter(java.util.function.Predicate<T> predicate, Consumer<T> consumer) {
        return t -> {
            if (predicate.test(t)) {
                consumer.accept(t);
            }
        };
    }

    /**
     * Creates a Function that applies a transformation and catches exceptions, returning a default value.
     * Combines mapping with safe execution.
     *
     * <p>Example:
     * <pre>{@code
     * Function<String, Integer> safeParseInt = Lambdas.map(
     *     Integer::parseInt,
     *     0  // default value on parse error
     * );
     *
     * Integer num1 = safeParseInt.apply("123");    // returns 123
     * Integer num2 = safeParseInt.apply("invalid"); // returns 0
     *
     * // With complex transformations
     * Function<String, User> loadUser = Lambdas.map(
     *     userId -> database.findUser(userId),
     *     User.guest()  // default guest user if not found
     * );
     * }</pre>
     *
     * @param function     the function to apply
     * @param defaultValue the default value if function throws exception
     * @param <T>          the input type
     * @param <R>          the output type
     * @return a safe Function with default value
     */
    public static <T, R> Function<T, R> map(Function<T, R> function, R defaultValue) {
        return t -> {
            try {
                return function.apply(t);
            } catch (Exception e) {
                return defaultValue;
            }
        };
    }

    /**
     * Executes a Runnable only if the condition is true.
     * Provides conditional execution in a functional style.
     *
     * <p>Example:
     * <pre>{@code
     * boolean isDebugMode = config.isDebugEnabled();
     *
     * Lambdas.runIf(isDebugMode, () -> {
     *     System.out.println("Debug information...");
     *     logDebugStats();
     * });
     *
     * // Inline conditions
     * Lambdas.runIf(user.isAdmin(), () -> grantAdminPrivileges());
     * }</pre>
     *
     * @param condition the condition to evaluate
     * @param runnable  the runnable to execute if condition is true
     */
    public static void runIf(boolean condition, Runnable runnable) {
        if (condition) {
            runnable.run();
        }
    }

    /**
     * Executes a Runnable only if the condition is false.
     * Opposite of runIf.
     *
     * <p>Example:
     * <pre>{@code
     * boolean hasCache = cache.isPresent();
     *
     * Lambdas.runUnless(hasCache, () -> {
     *     System.out.println("Cache miss, loading data...");
     *     loadDataFromSource();
     * });
     * }</pre>
     *
     * @param condition the condition to evaluate
     * @param runnable  the runnable to execute if condition is false
     */
    public static void runUnless(boolean condition, Runnable runnable) {
        if (!condition) {
            runnable.run();
        }
    }

    /**
     * Creates a Supplier that returns a default value if the original supplier returns null.
     * Provides null safety with automatic fallback.
     *
     * <p>Example:
     * <pre>{@code
     * Supplier<String> getName = Lambdas.orElse(
     *     () -> user.getName(),
     *     "Anonymous"
     * );
     *
     * String name = getName.get(); // returns "Anonymous" if user.getName() is null
     *
     * // With complex operations
     * Supplier<Config> config = Lambdas.orElse(
     *     () -> loadConfigFromFile(),
     *     Config.defaults()
     * );
     * }</pre>
     *
     * @param supplier     the supplier that may return null
     * @param defaultValue the default value if supplier returns null
     * @param <T>          the type of the result
     * @return a Supplier with null fallback
     */
    public static <T> Supplier<T> orElse(Supplier<T> supplier, T defaultValue) {
        return () -> {
            T value = supplier.get();
            return value != null ? value : defaultValue;
        };
    }

    /**
     * Creates a Supplier that tries the first supplier, and if it fails or returns null, tries the fallback.
     * Provides cascading fallback logic.
     *
     * <p>Example:
     * <pre>{@code
     * Supplier<String> config = Lambdas.orElseGet(
     *     () -> System.getProperty("app.config"),
     *     () -> loadDefaultConfig()
     * );
     *
     * // Multiple fallbacks can be chained
     * Supplier<Database> db = Lambdas.orElseGet(
     *     () -> connectToPrimary(),
     *     () -> connectToSecondary()
     * );
     * }</pre>
     *
     * @param primary  the primary supplier
     * @param fallback the fallback supplier
     * @param <T>      the type of the result
     * @return a Supplier with fallback
     */
    public static <T> Supplier<T> orElseGet(Supplier<T> primary, Supplier<T> fallback) {
        return () -> {
            try {
                T value = primary.get();
                if (value != null) {
                    return value;
                }
            } catch (Exception e) {
                // fall through to fallback
            }
            return fallback.get();
        };
    }
}

