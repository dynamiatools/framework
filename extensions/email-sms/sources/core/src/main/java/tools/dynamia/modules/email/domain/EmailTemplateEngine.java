package tools.dynamia.modules.email.domain;

/**
 * The language an {@link EmailTemplate} is written in.
 * <ul>
 *   <li>{@link #VELOCITY}: Apache Velocity through the platform {@code TemplateEngine}. It is what every template
 *   created before the block editor uses, so a template with no engine is a Velocity one.</li>
 *   <li>{@link #DYNAMIA}: the safe {@code DynamiaTemplateEngine} ({@code ${x}}, {@code {{x}}}, pipes, {@code if} and
 *   {@code each} blocks), which never runs code from the template.</li>
 * </ul>
 */
public enum EmailTemplateEngine {
    VELOCITY,
    DYNAMIA
}
