package mybookstore.actions;

import mybookstore.domain.Book;
import tools.dynamia.actions.ActionRuntime;
import tools.dynamia.actions.HeadlessCapable;
import tools.dynamia.actions.InstallAction;
import tools.dynamia.actions.RunsOn;
import tools.dynamia.crud.AbstractCrudAction;
import tools.dynamia.crud.CrudActionEvent;
import tools.dynamia.ui.UINavigation;

/** Demo of {@link UINavigation#openPage}: sends the user to the categories page. */
@InstallAction
@RunsOn(ActionRuntime.HEADLESS)
public class OpenCategoriesAction extends AbstractCrudAction implements HeadlessCapable {

    public OpenCategoriesAction() {
        setName("Go to Categories");
        setImage("tree");
        setApplicableClass(Book.class);
    }

    @Override
    public void actionPerformed(CrudActionEvent evt) {
        UINavigation.openPage("library/categories");
    }
}
