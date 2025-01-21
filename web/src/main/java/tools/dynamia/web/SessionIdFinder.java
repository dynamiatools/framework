package tools.dynamia.web;

import jakarta.servlet.http.HttpServletRequest;

/**
 * The interface Session id finder.
 */
public interface SessionIdFinder {

    String findSessionId(HttpServletRequest request);
}
