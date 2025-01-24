package tools.dynamia.web;

import jakarta.servlet.http.HttpServletRequest;
import tools.dynamia.integration.Containers;

/**
 * Default implementation of session id finder. Try to find current session id in
 * current request using cookie or http headers
 */
public class DefaultSessionIdFinder implements SessionIdFinder {

    private static SessionIdFinder INSTANCE = new DefaultSessionIdFinder();
    private static SessionIdFinder customFinder;

    public static SessionIdFinder getInstance() {
        return INSTANCE;
    }

    public static SessionIdFinder getCustomFinder() {
        if (customFinder == null) {
            customFinder = Containers.get().findObject(SessionIdFinder.class);
        }
        return customFinder;
    }

    public static void setCustomFinder(SessionIdFinder customFinder) {
        DefaultSessionIdFinder.customFinder = customFinder;
    }

    @Override
    public String findSessionId(HttpServletRequest req) {
        try {
            if (req != null) {
                Object sessionId = req.getAttribute(SessionStateStorage.SESSION_UUID_PARAM);
                if (sessionId == null) {
                    sessionId = findCookie(req, SessionStateStorage.SESSION_UUID_PARAM);
                }
                if (sessionId == null) {
                    //find in headers
                    sessionId = req.getHeader(SessionStateStorage.SESSION_UUID_PARAM);
                }

                if (sessionId != null) {
                    return sessionId.toString();
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * Try to find current session id in current request using cookie
     *
     * @return current session id
     */
    public String findCookie(HttpServletRequest request, String name) {
        var cookies = request.getCookies();
        if (cookies != null) {
            for (var cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
