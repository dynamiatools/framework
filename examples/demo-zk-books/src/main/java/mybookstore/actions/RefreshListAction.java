package mybookstore.actions;

import mybookstore.domain.Book;
import tools.dynamia.actions.ActionRuntime;
import tools.dynamia.actions.HeadlessCapable;
import tools.dynamia.actions.InstallAction;
import tools.dynamia.actions.RunsOn;
import tools.dynamia.crud.AbstractCrudAction;
import tools.dynamia.crud.CrudActionEvent;
import tools.dynamia.ui.MessageType;
import tools.dynamia.ui.UIMessages;
import tools.dynamia.ui.UINavigation;

/** Demo of {@link UINavigation#refresh}: reloads what the user is looking at. */
@InstallAction
@RunsOn(ActionRuntime.HEADLESS)
public class RefreshListAction extends AbstractCrudAction implements HeadlessCapable {

    public RefreshListAction() {
        setName("Refresh List");
        setImage("refresh");
        setApplicableClass(Book.class);
    }

    @Override
    public void actionPerformed(CrudActionEvent evt) {
        UIMessages.showMessage("Refreshing...", MessageType.INFO);
        UINavigation.refresh();
    }
}
