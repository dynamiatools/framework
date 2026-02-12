package tools.dynamia.web.navigation;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

/**
 * Interceptor interface for handling post-processing when the application root path (index) is accessed.
 * <p>
 * This functional interface allows implementations to intercept and customize behavior after the root path "/"
 * or index page is accessed in an MVC web application. It provides an opportunity to modify the model and view,
 * redirect users based on authentication state, inject common data, or perform any initialization logic before
 * the home/landing page is rendered.
 * </p>
 *
 * <p>
 * <b>Key use cases:</b>
 * <ul>
 *   <li>Redirecting authenticated users to their dashboard instead of public home</li>
 *   <li>Redirecting unauthenticated users to login page</li>
 *   <li>Adding application-wide context data to the index page model</li>
 *   <li>Performing analytics or logging for home page visits</li>
 *   <li>Loading and injecting featured content or announcements</li>
 *   <li>Detecting and redirecting mobile users to mobile-optimized views</li>
 *   <li>Setting up A/B testing variants for the landing page</li>
 *   <li>Implementing custom landing page routing based on user segments</li>
 * </ul>
 * </p>
 *
 * <p>
 * <b>Execution context:</b><br>
 * The interceptor is invoked <b>after</b> the index controller has processed the request and prepared
 * the {@link ModelAndView}, but <b>before</b> the view is rendered to the client. This allows modifications
 * to the model or view name to take effect in the final response.
 * </p>
 *
 * <p>
 * <b>Usage example:</b>
 * <pre>{@code
 * @Component
 * public class AuthenticatedUserRedirectInterceptor implements NavigationIndexInterceptor {
 *
 *     @Autowired
 *     private AuthenticationService authService;
 *
 *     @Override
 *     public void afterIndex(ModelAndView mv, HttpServletRequest request) {
 *         if (authService.isAuthenticated(request)) {
 *             // Redirect logged-in users to their dashboard
 *             mv.setViewName("redirect:/dashboard");
 *         } else {
 *             // Show public landing page for guests
 *             mv.addObject("showLoginPrompt", true);
 *         }
 *     }
 * }
 *
 * // Analytics example
 * @Component
 * public class IndexAnalyticsInterceptor implements NavigationIndexInterceptor {
 *
 *     @Autowired
 *     private AnalyticsService analyticsService;
 *
 *     @Override
 *     public void afterIndex(ModelAndView mv, HttpServletRequest request) {
 *         // Track landing page visit
 *         String referrer = request.getHeader("Referer");
 *         String userAgent = request.getHeader("User-Agent");
 *         analyticsService.trackLandingPageVisit(referrer, userAgent);
 *     }
 * }
 *
 * // Content injection example
 * @Component
 * public class FeaturedContentInterceptor implements NavigationIndexInterceptor {
 *
 *     @Autowired
 *     private ContentService contentService;
 *
 *     @Override
 *     public void afterIndex(ModelAndView mv, HttpServletRequest request) {
 *         // Load featured content for home page
 *         List<Article> featuredArticles = contentService.getFeaturedArticles();
 *         mv.addObject("featuredContent", featuredArticles);
 *
 *         // Add announcements
 *         Announcement announcement = contentService.getActiveAnnouncement();
 *         mv.addObject("announcement", announcement);
 *     }
 * }
 *
 * // Mobile redirect example
 * @Component
 * public class MobileDetectionInterceptor implements NavigationIndexInterceptor {
 *
 *     @Override
 *     public void afterIndex(ModelAndView mv, HttpServletRequest request) {
 *         String userAgent = request.getHeader("User-Agent");
 *         if (isMobileDevice(userAgent)) {
 *             mv.setViewName("redirect:/m/home");
 *         }
 *     }
 * }
 * }</pre>
 * </p>
 *
 * <p>
 * <b>Registration:</b><br>
 * Implementations annotated with {@code @Component} or {@code @Service} are automatically discovered
 * and registered by the framework. Multiple interceptors can be registered and will be executed in order
 * based on Spring's ordering mechanism ({@code @Order} annotation).
 * </p>
 *
 * <p>
 * <b>Note:</b> This interceptor is specifically for the root path ("/") or index page. For intercepting
 * navigation to other pages, use {@link PageNavigationInterceptor} instead.
 * </p>
 *
 * @author Mario A. Serrano Leones
 * @see ModelAndView
 * @see PageNavigationInterceptor
 */
@FunctionalInterface
public interface NavigationIndexInterceptor {

    /**
     * Invoked after the application index/root path is accessed.
     * <p>
     * This method is called after the index controller has prepared the {@link ModelAndView}, but before
     * the view is rendered. Implementations can modify the model, change the view name, or perform any
     * other post-processing logic for the landing page.
     * </p>
     *
     * <p>
     * <b>Common modifications:</b>
     * <ul>
     *   <li>Change view name: {@code mv.setViewName("redirect:/dashboard")}</li>
     *   <li>Add model attributes: {@code mv.addObject("key", value)}</li>
     *   <li>Clear model: {@code mv.getModel().clear()}</li>
     *   <li>Access session: {@code request.getSession().getAttribute("user")}</li>
     * </ul>
     * </p>
     *
     * @param mv the model and view prepared for the index page, can be modified
     * @param request the HTTP request that accessed the root path
     */
    void afterIndex(ModelAndView mv, HttpServletRequest request);
}
