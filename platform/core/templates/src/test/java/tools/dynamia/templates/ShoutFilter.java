package tools.dynamia.templates;

import java.util.Map;

/** Found through META-INF/services: proves that filters can be added without touching the engine. */
public class ShoutFilter implements TemplateFilter {
    @Override
    public String name() {
        return "shout";
    }

    @Override
    public Object apply(Object value, String argument, Map<String, Object> variables) {
        return value == null ? null : String.valueOf(value).toUpperCase() + (argument == null ? "!" : argument);
    }
}
