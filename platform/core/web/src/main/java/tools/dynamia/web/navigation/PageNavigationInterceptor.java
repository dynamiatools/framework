package tools.dynamia.web.navigation;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import tools.dynamia.navigation.Page;

/**
 * Interceptor interface for handling post-navigation events when pages are accessed directly via URI.
 * <p>
 * This functional interface allows implementations to intercept and perform custom logic after a {@link Page}
 * has been navigated to using a direct URL or path. It provides access to the page metadata, the model and view
 * being rendered, and the HTTP request/response objects, enabling a wide range of post-navigation processing
 * such as logging, analytics, authorization checks, content modification, or injecting additional model data.
 * </p>
 *
 * <p>
 * <b>Key use cases:</b>
 * <ul>
 *   <li>Analytics and tracking of page visits</li>
 *   <li>Logging navigation history and user activity</li>
 *   <li>Adding common model attributes or context data to all pages</li>
 *   <li>Performing authorization checks on protected pages</li>
 *   <li>Injecting user-specific or session-specific data into the model</li>
 *   <li>Modifying view names based on context (e.g., mobile vs desktop)</li>
 *   <li>Setting custom HTTP headers or cookies</li>
 *   <li>Redirecting or error handling based on page state</li>
 * </ul>
 * </p>
 *
 * <p>
 * <b>Interceptor execution:</b><br>
 * The interceptor is invoked <b>after</b> the page has been resolved and the {@link ModelAndView} has been
 * prepared, but <b>before</b> the view is rendered. This allows modifications to the model or view name
 * to take effect in the final rendering.
 * </p>
 *
 * <p>
 * <b>Usage example:</b>
 * <pre>{@code
 * @Component
 * public class PageAnalyticsInterceptor implements PageNavigationInterceptor {
 *
 *     @Autowired
 *     private AnalyticsService analyticsService;
 *
 *     @Override
 *     public void afterPage(Page page, ModelAndView modelAndView,
 *                           HttpServletRequest request, HttpServletResponse response) {
 *         // Track page visit
 *         String userId = (String) request.getSession().getAttribute("userId");
 *         analyticsService.trackPageView(page.getId(), userId, request.getRequestURI());
 *
 *         // Add page metadata to model
 *         modelAndView.addObject("pageTitle", page.getName());
 *         modelAndView.addObject("pageDescription", page.getDescription());
 *
 *         // Log navigation
 *         logger.info("User {} navigated to page: {}", userId, page.getName());
 *     }
 * }
 *
 * // Authorization example
 * @Component
 * public class PageAuthorizationInterceptor implements PageNavigationInterceptor {
 *
 *     @Override
 *     public void afterPage(Page page, ModelAndView modelAndView,
 *                           HttpServletRequest request, HttpServletResponse response) {
 *         if (page.isSecured() && !isUserAuthorized(request, page)) {
 *             modelAndView.setViewName("redirect:/access-denied");
 *         }
 *     }
 * }
 *
 * // Context injection example
 * @Component
 * public class UserContextInterceptor implements PageNavigationInterceptor {
 *
 *     @Override
 *     public void afterPage(Page page, ModelAndView modelAndView,
 *                           HttpServletRequest request, HttpServletResponse response) {
 *         User currentUser = getCurrentUser(request);
 *         modelAndView.addObject("currentUser", currentUser);
 *         modelAndView.addObject("userPreferences", currentUser.getPreferences());
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
 * @author Mario A. Serrano Leones
 * @see Page
 * @see ModelAndView
 * @see NavigationIndexInterceptor
 */
@FunctionalInterface
public interface PageNavigationInterceptor {

    /**
     * Invoked after a page has been navigated to via direct URI access.
     * <p>
     * This method is called after the page resolution is complete and the {@link ModelAndView} has been
     * prepared, but before the view is rendered. Implementations can modify the model, change the view name,
     * set HTTP headers, or perform any other post-navigation processing.
     * </p>
     *
     * <p>
     * <b>Parameters usage:</b>
     * <ul>
     *   <li><b>page:</b> Access page metadata (id, name, path, security settings)</li>
     *   <li><b>modelAndView:</b> Add/modify model attributes or change view name</li>
     *   <li><b>request:</b> Read request parameters, headers, session, or user information</li>
     *   <li><b>response:</b> Set response headers, cookies, or status codes</li>
     * </ul>
     * </p>
     *
     * @param page the page that was navigated to, containing page metadata and configuration
     * @param modelAndView the model and view being prepared for rendering, can be modified
     * @param request the HTTP request that initiated the navigation
     * @param response the HTTP response that will be sent to the client
     */
    void afterPage(Page page, ModelAndView modelAndView, HttpServletRequest request, HttpServletResponse response);
}
