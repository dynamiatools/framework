package tools.dynamia.modules.email;

/**
 * A variable that templates can print, offered to the editor so it can list it and show a sample in the preview.
 *
 * @param name   the path used in the template, for example {@code user.name}
 * @param label  a short human description, may be null
 * @param sample a value for the preview, may be null (the editor then shows the name)
 */
public record TemplateVariable(String name, String label, Object sample) {

    /**
     * @param name the variable path
     */
    public TemplateVariable(String name) {
        this(name, null, null);
    }
}
