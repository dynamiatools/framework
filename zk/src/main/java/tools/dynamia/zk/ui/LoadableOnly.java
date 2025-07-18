package tools.dynamia.zk.ui;

/**
 * The Interface LoadableOnly. Marks a component as loadable only.
 * This marker interface identifies UI components that are designed exclusively for loading
 * or displaying data without providing editing capabilities. Components implementing this
 * interface typically function as read-only viewers, data displays, or informational panels
 * in ZK-based user interfaces. It's commonly used in reporting interfaces, dashboard components,
 * and data visualization elements where user interaction is limited to viewing and navigation.
 * <br><br>
 * <b>Usage:</b><br>
 * <br>
 * <code>
 * public class DataViewer extends Div implements LoadableOnly {
 *     private List&lt;Object&gt; data;
 *     
 *     public void loadData(List&lt;Object&gt; newData) {
 *         this.data = newData;
 *         refreshDisplay();
 *     }
 *     
 *     // No editing methods - read-only component
 * }
 * 
 * // Framework can check for this interface
 * if (component instanceof LoadableOnly) {
 *     // Handle as read-only component
 *     disableEditingFeatures(component);
 * }
 * </code>
 *
 * @author Mario A. Serrano Leones
 */
public interface LoadableOnly {
}
