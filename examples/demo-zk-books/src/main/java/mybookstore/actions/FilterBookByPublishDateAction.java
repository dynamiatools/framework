package mybookstore.actions;

import mybookstore.domain.Book;
import tools.dynamia.actions.Control;
import tools.dynamia.actions.ActionRuntime;
import tools.dynamia.actions.InstallAction;
import tools.dynamia.actions.RunsOn;
import tools.dynamia.crud.actions.FilterAction;

/**
 * Filters the list by publish date. Declares the control and the field; ZK draws a date range box and a remote client draws
 * its own from the metadata, and the filtering is the same for both.
 */
@InstallAction
@RunsOn(ActionRuntime.FRONTEND)
public class FilterBookByPublishDateAction extends FilterAction {

    public FilterBookByPublishDateAction() {
        super(Book.class, Control.dateRange("publishDate").label("Publish date"));
        setAlwaysVisible(true);
    }
}
