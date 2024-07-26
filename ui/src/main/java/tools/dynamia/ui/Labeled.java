package tools.dynamia.ui;

public interface Labeled extends PropertiesContainer {

    default Labeled setLabel(String label) {
        setProperty("label", label);
        return this;
    }

    default String getLabel() {
        return getProperty("label");
    }
}
