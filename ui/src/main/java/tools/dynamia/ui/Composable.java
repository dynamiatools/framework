package tools.dynamia.ui;

import java.util.List;

public interface Composable extends PropertiesContainer {

    void add(Composable composable);

    List<Composable> getChildren();


}
