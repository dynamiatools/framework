package tools.dynamia.navigation;

/**
 * General navigation runtime exception
 */
public class NavigationException extends RuntimeException {

    public NavigationException(String message) {
        super(message);
    }

    public NavigationException(String message, Throwable cause) {
        super(message, cause);
    }


}
