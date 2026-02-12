package tools.dynamia.domain;

/**
 * The Interface LazyLoadable. Represents objects that support lazy loading of their data.
 * This interface provides a standardized mechanism for implementing deferred data loading patterns,
 * commonly used in ORM frameworks, proxy objects, and performance optimization scenarios. Lazy loading
 * helps reduce memory consumption and improve application startup time by loading data only when needed.
 * It's particularly useful for large datasets, expensive computations, or remote data access operations.
 * <br><br>
 * <b>Usage:</b><br>
 * <br>
 * <code>
 * public class LazyCollection implements LazyLoadable {
 *     private List&lt;Item&gt; items;
 *     private boolean loaded = false;
 *     
 *     public void lazyLoad() {
 *         if (!loaded) {
 *             items = dataService.loadItems();
 *             loaded = true;
 *         }
 *     }
 *     
 *     public List&lt;Item&gt; getItems() {
 *         lazyLoad();
 *         return items;
 *     }
 * }
 * </code>
 *
 * @author Mario A. Serrano Leones
 */
public interface LazyLoadable {

    /**
     * Performs lazy loading of the object's data.
     */
    void lazyLoad();
}
