package mybookstore.actions;

import mybookstore.domain.Book;
import tools.dynamia.actions.Control;
import tools.dynamia.actions.ActionRuntime;
import tools.dynamia.actions.InstallAction;
import tools.dynamia.actions.RunsOn;
import tools.dynamia.crud.actions.FilterAction;

/** Filters the list by a text found in the title. */
@InstallAction
@RunsOn(ActionRuntime.FRONTEND)
public class FilterBookByTitleAction extends FilterAction {

    public FilterBookByTitleAction() {
        super(Book.class, Control.text("title").label("Title contains"));
        setName("Title");
        setAlwaysVisible(true);
    }
}
