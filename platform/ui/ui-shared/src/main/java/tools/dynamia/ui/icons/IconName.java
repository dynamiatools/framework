package tools.dynamia.ui.icons;

import java.util.List;

/**
 * Immutable record representing an icon name and its associated CSS classes.
 *
 * <p>This class is used to encapsulate the logical name of an icon along with any additional
 * CSS classes that should be applied when rendering the icon. The name typically corresponds
 * to a key in an IconsProvider, while the classes can be used for styling purposes.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * IconName iconName = new IconName("edit", List.of("red", "bold"));
 * String name = iconName.name(); // "edit"
 * List<String> classes = iconName.classes(); // ["red", "bold"]
 * }</pre>
 *
 * @author Mario A. Serrano Leones
 */
public record IconName(String name, List<String> classes) {


}
