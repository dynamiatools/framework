package tools.dynamia.web;

import jakarta.servlet.http.HttpServletRequest;
import tools.dynamia.integration.Containers;
import tools.dynamia.web.util.HttpUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * State storage API to delegate distributed state storage to a {@link SessionStateStorage} implementation.
 * <p>
 * By default use a {@link MapSessionStateStorage} as backend.
 * <p>
 * You can replace the backend implementation using CDI {@link SessionStateStorage} qualifier.
 */
public interface SessionStateStorage {

    String SESSION_UUID_PARAM = "session-uuid";
    String AUTHORIZATION_HEADER = "Authorization";
    String BEARER_TOKEN = "Bearer ";

    /**
     * Find the current implementation (spring bean) or return a {@link MapSessionStateStorage} DEFAULT
     *
     * @return
     */
    static SessionStateStorage getCurrent() {
        SessionStateStorage storage = Containers.get().findObject(SessionStateStorage.class);
        if (storage == null) {
            storage = MapSessionStateStorage.DEFAULT;
        }
        return storage;
    }

    /**
     * Try to find current session id in current request
     *
     * @return current session id
     */
    static String findCurrentSessionId() {
        return findCurrentSessionId(HttpUtils.getCurrentRequest());
    }

    /**
     * Try to find current session id
     *
     * @return current session id
     */
    static String findCurrentSessionId(HttpServletRequest req) {
        try {
            if (req != null) {
                Object sessionId = req.getAttribute(SESSION_UUID_PARAM);
                if (sessionId == null) {
                    sessionId = findCookie(req, SESSION_UUID_PARAM);
                }
                if (sessionId == null) {
                    //find in headers
                    sessionId = req.getHeader(SessionStateStorage.SESSION_UUID_PARAM);
                }

                if (sessionId == null) {
                    //find in authorization bearer
                    String authorization = req.getHeader(AUTHORIZATION_HEADER);
                    if (authorization != null && authorization.startsWith(BEARER_TOKEN)) {
                        sessionId = authorization.substring(7);
                    }
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


    private static String findCookie(HttpServletRequest request, String tokenName) {
        var cookies = request.getCookies();
        if (cookies != null) {
            for (var cookie : cookies) {
                if (cookie.getName().equals(tokenName)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * Put a value in session
     *
     * @param sessionId
     * @param key
     * @param value
     */
    void put(String sessionId, String key, String value);

    /**
     * Get a value from session
     *
     * @param sessionId id of session
     * @param key
     * @return
     */
    String get(String sessionId, String key);

    /**
     * Get a value from session or return a default value
     *
     * @param sessionId
     * @param key
     * @param defaultValue
     * @return
     */
    String get(String sessionId, String key, String defaultValue);

    /**
     * Get all keys from session
     *
     * @param sessionId
     * @return
     */
    Set<String> getKeys(String sessionId);

    /**
     * Get all values from session
     *
     * @param sessionId
     * @return
     */
    Map<String, String> getAllValues(String sessionId);

    /**
     * Remove a value from session
     *
     * @param sessionId
     * @param key
     */
    void remove(String sessionId, String key);

    /**
     * Get all sessions ids
     *
     * @return
     */
    List<String> getSessions();

    /**
     * Remove a session
     *
     * @param sessionId id
     */
    void removeSession(String sessionId);

    /**
     * Create a new session
     *
     * @return sessionId
     */
    String newSession();


}
