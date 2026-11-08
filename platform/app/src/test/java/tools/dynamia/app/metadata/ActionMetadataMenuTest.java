package tools.dynamia.app.metadata;

import org.junit.jupiter.api.Test;
import tools.dynamia.crud.AbstractCrudAction;
import tools.dynamia.crud.CrudActionEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ActionMetadataMenuTest {

    static class InMenu extends AbstractCrudAction {
        InMenu() {
            setMenuSupported(true);
        }

        @Override
        public void actionPerformed(CrudActionEvent evt) {
        }
    }

    static class NotInMenu extends AbstractCrudAction {
        @Override
        public void actionPerformed(CrudActionEvent evt) {
        }
    }

    @Test
    void publishesThePositionOfTheAction() {
        var action = new InMenu();
        action.setPosition(2.5);
        assertEquals(2.5, new ActionMetadata(action).getPosition());
    }

    @Test
    void publishesWhetherACrudActionIsListedInTheRowMenu() {
        assertTrue(new ActionMetadata(new InMenu()).isMenuSupported());
        assertFalse(new ActionMetadata(new NotInMenu()).isMenuSupported());
    }
}
