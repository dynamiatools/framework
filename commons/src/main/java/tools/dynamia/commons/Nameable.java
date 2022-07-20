package tools.dynamia.commons;

public interface Nameable {

    default String toName() {
        return toString();
    }

    default void name(String name) {

    }
}
