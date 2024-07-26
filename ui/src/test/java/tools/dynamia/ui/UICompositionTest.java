package tools.dynamia.ui;

import org.junit.Assert;
import org.junit.Test;

public class UICompositionTest {

    @Test
    public void should_be_declarative() {

        var label = new Label("El Mario");



        Assert.assertEquals(label.getLabel(), "El Mario");
        Assert.assertEquals(label.getSize(), "large");

    }
}
