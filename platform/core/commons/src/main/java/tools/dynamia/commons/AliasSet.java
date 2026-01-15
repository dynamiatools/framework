package tools.dynamia.commons;

import java.lang.annotation.*;

/**
 * Container annotation representing multiple {@link Alias}
 * annotations on a single target.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface AliasSet {
    Alias[] value();
}
