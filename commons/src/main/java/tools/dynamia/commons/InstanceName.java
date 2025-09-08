package tools.dynamia.commons;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a field or method as the representative name of an instance.
 * <p>
 * Used in models and beans to indicate which property should be used as the display name or identifier for UI, logging, or serialization purposes.
 * Utility classes like {@link BeanUtils} can use this annotation to extract a human-readable name for an object.
 * <p>
 * Example usage:
 * <pre>
 * {@code
 * @InstanceName
 * private String fullName;
 * }
 * </pre>
 *
 * @author Mario A. Serrano Leones
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface InstanceName {
}
