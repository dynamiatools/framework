package tools.dynamia.web;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Very simple basic implementation that delegate all methods to a {@link ConcurrentHashMap}
 */
public class MapSessionStateStorage implements SessionStateStorage {

    public static final SessionStateStorage DEFAULT = new MapSessionStateStorage();
    private final Map<String, Map<String, String>> storage = new ConcurrentHashMap<>();


    @Override
    public void put(String sessionId, String key, String value) {
        getSession(sessionId).put(key, value);
    }

    private Map<String, String> getSession(String sessionId) {
        var session = storage.getOrDefault(sessionId, new ConcurrentHashMap<>());
        storage.putIfAbsent(sessionId, session);
        return session;
    }

    @Override
    public String get(String sessionId, String key) {
        return getSession(sessionId).get(key);
    }

    @Override
    public String get(String sessionId, String key, String defaultValue) {
        return getSession(sessionId).getOrDefault(key, defaultValue);
    }

    @Override
    public Set<String> getKeys(String sessionId) {
        return getSession(sessionId).keySet();
    }

    @Override
    public Map<String, String> getAllValues(String sessionId) {
        return getSession(sessionId);
    }

    @Override
    public void remove(String sessionId, String key) {
        getSession(sessionId).remove(key);
    }

    @Override
    public List<String> getSessions() {
        return storage.keySet().stream().toList();
    }

    @Override
    public void removeSession(String sessionId) {
        storage.remove(sessionId);
    }

    @Override
    public String newSession() {
        String sessionId = "HM" + UUID.randomUUID();
        getSession(sessionId);
        return sessionId;
    }

    @Override
    public boolean isValidSession(String sessionId) {
        return storage.containsKey(sessionId);
    }
}
