package tools.dynamia.ui;

public interface Sizable extends PropertiesContainer {

    default Sizable setSize(String size) {
        setProperty("size", size);
        return this;
    }

    default String getSize() {
        return getProperty("size");
    }
}
