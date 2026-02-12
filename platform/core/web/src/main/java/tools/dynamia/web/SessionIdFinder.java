package tools.dynamia.web;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Strategy interface for extracting session identifiers from HTTP requests.
 *
 * <p>This interface defines a contract for locating and extracting session IDs from various
 * sources within an HTTP request. Different implementations can provide different strategies
 * for finding session identifiers based on application requirements.</p>
 *
 * <p>Common sources for session IDs include:</p>
 * <ul>
 *   <li>HTTP headers (e.g., Authorization, X-Session-ID)</li>
 *   <li>Cookies (e.g., JSESSIONID)</li>
 *   <li>URL parameters or query strings</li>
 *   <li>Request attributes</li>
 *   <li>Custom authentication tokens</li>
 * </ul>
 *
 * <p>Use cases include:</p>
 * <ul>
 *   <li>Custom session management for REST APIs</li>
 *   <li>Integration with external authentication systems</li>
 *   <li>Support for token-based authentication</li>
 *   <li>Custom session tracking mechanisms</li>
 * </ul>
 *
 * <p>Example implementation extracting session ID from a custom header:</p>
 * <pre>{@code
 * @Component
 * public class HeaderSessionIdFinder implements SessionIdFinder {
 *
 *     @Override
 *     public String findSessionId(HttpServletRequest request) {
 *         return request.getHeader("X-Session-ID");
 *     }
 * }
 * }</pre>
 *
 * <p>Example implementation with fallback strategy:</p>
 * <pre>{@code
 * @Component
 * public class MultiSourceSessionIdFinder implements SessionIdFinder {
 *
 *     @Override
 *     public String findSessionId(HttpServletRequest request) {
 *         // Try header first
 *         String sessionId = request.getHeader("X-Session-ID");
 *
 *         // Fallback to cookie
 *         if (sessionId == null && request.getCookies() != null) {
 *             for (Cookie cookie : request.getCookies()) {
 *                 if ("JSESSIONID".equals(cookie.getName())) {
 *                     sessionId = cookie.getValue();
 *                     break;
 *                 }
 *             }
 *         }
 *
 *         // Fallback to HTTP session
 *         if (sessionId == null && request.getSession(false) != null) {
 *             sessionId = request.getSession(false).getId();
 *         }
 *
 *         return sessionId;
 *     }
 * }
 * }</pre>
 *
 * @see HttpServletRequest
 * @see HttpServletRequest#getSession()
 */
public interface SessionIdFinder {

    /**
     * Extracts the session identifier from the given HTTP request.
     *
     * <p>Implementations should examine the request and locate the session ID using
     * their specific strategy. The method should return {@code null} if no session
     * identifier can be found in the request.</p>
     *
     * <p>This method is typically called during request processing to identify
     * the user's session for authentication, authorization, or session tracking purposes.</p>
     *
     * @param request the HTTP servlet request containing the potential session identifier
     * @return the session ID extracted from the request, or {@code null} if no session ID is found
     *
     * @see HttpServletRequest#getHeader(String)
     * @see HttpServletRequest#getCookies()
     * @see HttpServletRequest#getSession(boolean)
     */
    String findSessionId(HttpServletRequest request);
}
