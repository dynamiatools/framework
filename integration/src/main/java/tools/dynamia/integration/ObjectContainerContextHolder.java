package tools.dynamia.integration;

public class ObjectContainerContextHolder {

    private static final ThreadLocal<ObjectContainer> context = new ThreadLocal<>();

    public static void set(ObjectContainer container) {
        context.set(container);
    }

    public static ObjectContainer get() {
        return context.get();
    }

    public static void clear() {
        context.remove();
    }
}
