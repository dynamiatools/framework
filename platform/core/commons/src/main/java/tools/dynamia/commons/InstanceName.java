package tools.dynamia.commons;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field or a public no-arg method as the name of the instance, that is, the text that represents the object
 * to users instead of {@code toString()}. Resolved by {@link ObjectOperations#getInstanceName(Object)}, which falls
 * back to {@code toString()} when no marked member exists or its value is {@code null} or blank. It is used by the UI
 * (labels, pickers, table cells) and by {@link BeanMap} as its string representation.
 * <p>
 * Example:
 * <pre>{@code
 * public class Customer {
 *     @InstanceName
 *     private String fullName;
 * }
 * }</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface InstanceName {
}
