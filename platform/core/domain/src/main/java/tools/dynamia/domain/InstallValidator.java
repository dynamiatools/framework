package tools.dynamia.domain;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * Annotation to mark a class as an installable {@link Validator} in the application context.
 * <p>
 * Classes annotated with {@code InstallValidator} are automatically registered as Spring components
 * with prototype scope, allowing them to be instantiated and managed by the framework as validators.
 * This annotation is typically used to facilitate the dynamic discovery and installation of validators
 * in modular or plugin-based systems.
 * </p>
 * <p>
 * Example usage:
 * <pre>
 *     @InstallValidator
 *     public class MyCustomAction implements Validator {
 *         // Implementation details
 *     }
 * </pre>
 * </p>
 *
 * @author Mario A. Serrano Leones
 */
@Target(ElementType.TYPE)
@Component
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface InstallValidator {
}
