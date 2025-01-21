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

    /**
     * Find the current implementation (spring bean) or return a {@link MapSessionStateStorage} DEFAULT
     *
     * @return current implementation or a {@link MapSessionStateStorage} DEFAULT
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
     * Try to find current session id in current request using cookie or http header
     *
     * @return current session id
     */
    static String findCurrentSessionId(HttpServletRequest req) {
        SessionIdFinder sessionIdFinder = DefaultSessionIdFinder.getCustomFinder();
        if (sessionIdFinder == null) {
            sessionIdFinder = DefaultSessionIdFinder.getInstance();
        }

        return sessionIdFinder.findSessionId(req);
    }


    /**
     * Put a value in session
     *
     * @param sessionId id of session
     * @param key       key to put
     * @param value     value to put
     */
    void put(String sessionId, String key, String value);

    /**
     * Put multiple values in session
     *
     * @param sessionId id of session
     * @param values    values to put
     */
    default void put(String sessionId, Map<String, String> values) {
        values.forEach((k, v) -> put(sessionId, k, v));
    }

    /**
     * Get a value from session
     *
     * @param sessionId id of session
     * @param key       key to get
     * @return value or null if not exists
     */
    String get(String sessionId, String key);

    /**
     * Get a value from session or return a default value
     *
     * @param sessionId    id of session
     * @param key          key to get
     * @param defaultValue default value to return if key not exists
     * @return value or defaultValue if not exists
     */
    String get(String sessionId, String key, String defaultValue);

    /**
     * Get all keys from session
     *
     * @param sessionId id of session
     * @return keys or empty list if not exists
     */
    Set<String> getKeys(String sessionId);

    /**
     * Get all values from session
     *
     * @param sessionId id of session
     * @return values or empty map if not exists
     */
    Map<String, String> getAllValues(String sessionId);

    /**
     * Remove a value from session
     *
     * @param sessionId id of session
     * @param key       key to remove
     */
    void remove(String sessionId, String key);

    /**
     * Get all sessions ids
     *
     * @return sessions ids
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

    /**
     * Check if session is valid
     * @param sessionId session id to check
     * @return true if session is valid
     */
    boolean isValidSession(String sessionId);


}
