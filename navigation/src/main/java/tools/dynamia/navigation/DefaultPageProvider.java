package tools.dynamia.navigation;

/**
 * Default page provider return the home page path
 */
@FunctionalInterface
public interface DefaultPageProvider {

    String getPath();
}
