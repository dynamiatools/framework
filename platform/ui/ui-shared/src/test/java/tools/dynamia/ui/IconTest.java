package tools.dynamia.ui;

import org.junit.Test;
import tools.dynamia.ui.icons.IconName;
import tools.dynamia.ui.icons.Icons;

public class IconTest {

    @Test
    public void shouldParseIconName() {
        IconName iconName = Icons.parseIconName("edit|red,blue,green");
        assert iconName.name().equals("edit");
        assert iconName.classes().size() == 3;
        assert iconName.classes().get(0).equals("red");
        assert iconName.classes().get(1).equals("blue");
        assert iconName.classes().get(2).equals("green");

    }
}
