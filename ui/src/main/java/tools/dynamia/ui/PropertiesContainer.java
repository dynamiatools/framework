package tools.dynamia.ui;

import java.util.Map;

public interface PropertiesContainer {

    <T> void setProperty(String name, T value);

    <T> T getProperty(String name);

    Map<String, Object> getProperties();
}
