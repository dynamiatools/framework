package tools.dynamia.ui;

public interface Alignable extends PropertiesContainer {

    default Alignable setAlign(String align) {
        setProperty("align", align);
        return this;
    }

    default String getAlign() {
        return getProperty("align");
    }
}
