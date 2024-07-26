package tools.dynamia.ui;

import java.util.*;

public class BaseComponent implements Composable {

    private List<Composable> children = new LinkedList<>();
    private Map<String, Object> properties = new HashMap<>();


    @Override
    public void add(Composable composable) {
        children.add(composable);
    }


    @Override
    public List<Composable> getChildren() {
        return children.stream().toList();
    }

    @Override
    public <T> void setProperty(String name, T value) {
        properties.put(name, value);
    }

    @Override
    public <T> T getProperty(String name) {
        return (T) properties.get(name);
    }

    @Override
    public Map<String, Object> getProperties() {
        return Collections.unmodifiableMap(properties);
    }
}
