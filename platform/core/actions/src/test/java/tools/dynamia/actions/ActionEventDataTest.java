package tools.dynamia.actions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ActionEventDataTest {

    @Test
    void getDataWithTypeReturnsTheDataWhenItMatches() {
        ActionEvent evt = new ActionEvent("hello", this);
        assertEquals("hello", evt.getData(String.class));
        assertEquals("hello", evt.getData(CharSequence.class));
    }

    @Test
    void getDataWithTypeReturnsNullInsteadOfFailing() {
        assertNull(new ActionEvent("hello", this).getData(Integer.class));
        assertNull(new ActionEvent(null, this).getData(String.class));
        assertNull(new ActionEvent("hello", this).getData((Class<String>) null));
    }
}
