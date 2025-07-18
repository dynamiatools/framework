package tools.dynamia.domain;

/**
 * The Interface LazyLoadable. Represents objects that support lazy loading of their data.
 *
 * @author Mario A. Serrano Leones
 */
public interface LazyLoadable {

    /**
     * Performs lazy loading of the object's data.
     */
    void lazyLoad();
}
