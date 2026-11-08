package mybookstore.actions;

import mybookstore.domain.Book;
import tools.dynamia.actions.ActionRuntime;
import tools.dynamia.actions.HeadlessCapable;
import tools.dynamia.actions.InstallAction;
import tools.dynamia.actions.RunsOn;
import tools.dynamia.crud.AbstractCrudAction;
import tools.dynamia.crud.CrudActionEvent;
import tools.dynamia.ui.MessageType;
import tools.dynamia.ui.UIChoices;
import tools.dynamia.ui.UIMessages;

/**
 * Demo of {@link UIChoices#pickEntity}: lets the user search the {@code books} lookup and pick one; the lookup
 * ({@link mybookstore.providers.BookLookup}) decides what can be found.
 */
@InstallAction
@RunsOn(ActionRuntime.HEADLESS)
public class PickSimilarBookAction extends AbstractCrudAction implements HeadlessCapable {

    public PickSimilarBookAction() {
        setName("Pick Similar Book");
        setImage("search");
        setApplicableClass(Book.class);
    }

    @Override
    public void actionPerformed(CrudActionEvent evt) {
        UIChoices.<Book>pickEntity("books", "Pick a similar book", picked ->
                UIMessages.showMessage("Similar book: " + picked.getTitle(), MessageType.INFO));
    }
}
