package tools.dynamia.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import tools.dynamia.commons.StringPojoParser;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.domain.AbstractEntity;
import tools.dynamia.integration.Containers;
import tools.dynamia.web.util.HttpUtils;

import java.time.temporal.Temporal;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * State storage API to delegate distributed state storage to a {@link SessionStateStorage} implementation.
 * <p>
 * By default use a {@link MapSessionStateStorage} as backend.
 * <p>
 * You can replace the backend implementation using a {@link tools.dynamia.integration.sterotypes.Component} qualifier.
 */
public interface SessionStateStorage {

    LoggingService LOGGER = new SLF4JLoggingService(SessionStateStorage.class);

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
     * Put a value in current session
     *
     * @param key   key to put
     * @param value value to put
     */
    static void putInSession(String key, String value) {
        try {
            getCurrent().put(findCurrentSessionId(), key, value);
        } catch (Exception e) {
            LOGGER.error("Error putting value in session: " + key + " = " + value, e);
        }
    }

    /**
     * Put a number in current session. It will be converted to string
     *
     * @param key   key to put
     * @param value value to put
     */
    static void putInSession(@NotNull String key, @NotNull Number value) {
        putInSession(key, value.toString());
    }

    /**
     * Put a temporal in current session. It will be converted to string
     *
     * @param key   key to put
     * @param value value to put
     */
    static void putInSession(@NotNull String key, @NotNull Temporal value) {
        putInSession(key, value.toString());
    }

    /**
     * Put a boolean in current session. It will be converted to string
     *
     * @param key   key to put
     * @param value value to put
     */
    static void putInSession(@NotNull String key, boolean value) {
        putInSession(key, String.valueOf(value));
    }

    /**
     * Put a pojo in current session. It will be converted to json string
     *
     * @param key  key to put
     * @param pojo value to put
     */
    static void putInSession(@NotNull String key, @NotNull Object pojo) {
        putInSession(key, StringPojoParser.convertPojoToJson(pojo));
    }

    /**
     * Put multiple values in current session
     *
     * @param values values to put
     */
    static void putInSession(Map<String, String> values) {
        try {
            getCurrent().put(findCurrentSessionId(), values);
        } catch (Exception e) {
            LOGGER.error("Error putting values in session: " + values, e);
        }
    }

    /**
     * Get a value from current session
     *
     * @param key key to get
     * @return value or null if not exists
     */
    static String getFromSession(String key) {
        try {
            return getCurrent().get(findCurrentSessionId(), key);
        } catch (Exception e) {
            LOGGER.error("Error getting value from session. Returning null. key = " + key, e);
            return null;
        }
    }

    /**
     * Remove value from current session
     *
     * @param key key to remove
     */
    static void removeFromSession(String key) {
        try {
            getCurrent().remove(findCurrentSessionId(), key);
        } catch (Exception e) {
            LOGGER.error("Error removing value from session: " + key, e);
        }
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
     *
     * @param sessionId session id to check
     * @return true if session is valid
     */
    boolean isValidSession(String sessionId);


}
