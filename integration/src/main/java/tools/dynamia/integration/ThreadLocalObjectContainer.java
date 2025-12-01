package tools.dynamia.integration;

import java.util.Collection;

public class ThreadLocalObjectContainer {

    private static final ThreadLocal<ObjectContainer> context = new ThreadLocal<>();

    /**
     * Sets the object container for the current thread.
     *
     * @param container the object container to set
     */
    public static void set(ObjectContainer container) {
        context.set(container);
    }

    /**
     * Gets the object container for the current thread.
     *
     * @return the object container
     */
    public static ObjectContainer get() {
        return context.get();
    }

    /**
     * Clears the object container for the current thread.
     */
    public static void clear() {
        context.remove();
    }

    /**
     * Initializes a new SimpleObjectContainer for the current thread.
     */
    public static void init() {
        context.set(new SimpleObjectContainer());
    }

    /**
     * Retrieves an object of the specified class from the current thread's object container.
     * If not found, it falls back to the global Containers instance.
     *
     * @param clazz the class of the object to retrieve
     * @param <T>   the type of the object
     * @return the retrieved object, or null if not found
     */
    public static <T> T getObject(Class<T> clazz) {
        ObjectContainer context = get();
        if (context != null) {
            T obj = context.getObject(clazz);
            if (obj != null) return obj;
        }

        return Containers.get().findObject(clazz);
    }

    /**
     * Retrieves an object by name and class from the current thread's object container.
     * If not found, it falls back to the global Containers instance.
     *
     * @param name  the name of the object to retrieve
     * @param clazz the class of the object to retrieve
     * @param <T>   the type of the object
     * @return the retrieved object, or null if not found
     */
    public static <T> T getObject(String name, Class<T> clazz) {
        ObjectContainer context = get();
        if (context != null) {
            T obj = context.getObject(name, clazz);
            if (obj != null) return obj;
        }

        return Containers.get().findObject(name, clazz);
    }

    /**
     * Retrieves all objects of the specified class from the current thread's object container.
     * If none are found, it falls back to the global Containers instance.
     *
     * @param clazz the class of the objects to retrieve
     * @param <T>   the type of the objects
     * @return a collection of retrieved objects
     */
    public static <T> Collection<T> getObjects(Class<T> clazz) {
        ObjectContainer context = get();
        if (context != null) {
            Collection<T> objs = context.getObjects(clazz);
            if (objs != null && !objs.isEmpty()) return objs;
        }

        return Containers.get().findObjects(clazz);
    }

    /**
     * Checks if the object container for the current thread is initialized.
     *
     * @return true if initialized, false otherwise
     */
    public static boolean isInitialized() {
        return context.get() != null;
    }

    public static void copyTo(SimpleObjectContainer target) {
        if (context.get() instanceof SimpleObjectContainer scon) {
            target.getObjects().putAll(scon.getObjects());
        }
    }
}
