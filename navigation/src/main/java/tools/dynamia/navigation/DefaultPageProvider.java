package tools.dynamia.navigation;

/**
 * <p>
 * The <b>DefaultPageProvider</b> interface defines a contract for supplying the default (home) page path in a navigation system. Implementations of this interface are responsible for returning the path to the application's default or landing page, which is typically used for initial navigation or as a fallback when no specific page is requested.
 * </p>
 *
 * <h2>Usage Example</h2>
 * <pre>
 * // Registering as a Spring Bean in a Spring Boot application
 * @SpringBootApplication
 * public class MyApplication {
 *     public static void main(String[] args) {
 *         SpringApplication.run(MyApplication.class, args);
 *     }
 *
 *     @Bean
 *     public DefaultPageProvider defaultPageProvider() {
 *         return () -> "/home";
 *     }
 * }
 *
 * // Usage in navigation framework
 * @Autowired
 * private DefaultPageProvider defaultPageProvider;
 *
 * String homePath = defaultPageProvider.getPath();
 * </pre>
 *
 * <h2>Typical Scenarios</h2>
 * <ul>
 *   <li>Providing the home page path for navigation frameworks</li>
 *   <li>Customizing the landing page in modular applications</li>
 *   <li>Testing navigation logic by supplying mock default paths</li>
 *   <li>Registering as a Spring Bean for dependency injection</li>
 * </ul>
 *
 * <h2>Thread Safety</h2>
 * <p>
 * Implementations should ensure thread safety if the provider is accessed concurrently.
 * </p>
 *
 * @author Mario A. Serrano Leones
 */
@FunctionalInterface
public interface DefaultPageProvider {

    /**
     * Returns the path to the default (home) page of the application.
     * <p>
     * This method is called by navigation frameworks or consumers to obtain the path for initial navigation or as a fallback.
     * </p>
     *
     * <h3>Example</h3>
     * <pre>
     * // Registering as a Spring Bean
     * @Bean
     * public DefaultPageProvider defaultPageProvider() {
     *     return () -> "/home";
     * }
     *
     * // Usage
     * String homePath = defaultPageProvider.getPath();
     * </pre>
     *
     * @return the path to the default page (e.g., "/home")
     */
    String getPath();
}
