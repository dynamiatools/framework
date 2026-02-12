package tools.dynamia.commons;

import java.lang.annotation.*;

/**
 * Represents one or more semantic aliases for a class or field.
 * Aliases can be used for DTO generation, external mapping,
 * multi-locale naming, versioning, or backward compatibility.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
@Repeatable(AliasSet.class)
public @interface Alias {

    /**
     * Primary alias values for the resource.
     * Multiple values are allowed because different contexts
     * may treat them as "main names".
     */
    String[] value();


    /**
     * Intended usage or semantic scope of the alias.
     * Examples: "dto", "external", "legacy", "csv", "graphql", "pos", "erp"
     */
    String scope() default "default";

    /**
     * Locale for this alias, following BCP 47 style.
     * Examples: "en_US", "es_CO", "pt_BR"
     */
    String locale() default "";

    /**
     * Version of the alias, useful for API evolution.
     * Example: "v1", "v2".
     */
    String version() default "";

    /**
     * Priority ordering for conflict resolution.
     * Higher values override lower ones.
     */
    int priority() default 0;
}
