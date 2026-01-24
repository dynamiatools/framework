/**
 * Reactive programming framework inspired by React and Vue.
 *
 * <p>This package provides a lightweight reactive system for Java that automatically
 * tracks dependencies and propagates changes through your application.</p>
 *
 * <h2>Core Concepts</h2>
 *
 * <h3>Reactive References (Ref)</h3>
 * <p>Mutable reactive values that notify subscribers when changed:</p>
 * <pre>{@code
 * Ref<String> name = Reactive.ref("John");
 * name.set("Jane"); // Triggers all dependent effects
 * }</pre>
 *
 * <h3>Computed Values</h3>
 * <p>Derived values that automatically update when dependencies change:</p>
 * <pre>{@code
 * Ref<Integer> width = Reactive.ref(10);
 * Ref<Integer> height = Reactive.ref(20);
 * Computed<Integer> area = Reactive.computed(() -> width.get() * height.get());
 * // area automatically updates when width or height changes
 * }</pre>
 *
 * <h3>Effects</h3>
 * <p>Side effects that automatically re-run when dependencies change:</p>
 * <pre>{@code
 * Ref<String> message = Reactive.ref("Hello");
 * Reactive.effect(() -> {
 *     System.out.println(message.get());
 * }); // Prints "Hello", re-runs when message changes
 * }</pre>
 *
 * <h2>Features</h2>
 * <ul>
 *   <li>Automatic dependency tracking</li>
 *   <li>Lazy evaluation of computed values</li>
 *   <li>Thread-safe operations</li>
 *   <li>No manual subscription management</li>
 *   <li>Conditional dependencies support</li>
 * </ul>
 *
 * <h2>Example Usage</h2>
 * <pre>{@code
 * // Create state
 * Ref<Integer> count = Reactive.ref(0);
 * Ref<Integer> multiplier = Reactive.ref(2);
 *
 * // Create computed values
 * Computed<Integer> doubled = Reactive.computed(() ->
 *     count.get() * multiplier.get()
 * );
 *
 * // Create side effects
 * Reactive.effect(() -> {
 *     System.out.println("Result: " + doubled.get());
 * });
 *
 * // Update state - effects run automatically
 * count.set(5); // Prints: "Result: 10"
 * multiplier.set(3); // Prints: "Result: 15"
 * }</pre>
 *
 * @since 5.0.0
 */
package tools.dynamia.integration.reactive;
