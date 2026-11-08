package mybookstore.actions;

import mybookstore.domain.Book;
import tools.dynamia.actions.ActionRuntime;
import tools.dynamia.actions.InstallAction;
import tools.dynamia.actions.RunsOn;
import tools.dynamia.commons.LocalDateRange;
import tools.dynamia.crud.AbstractCrudAction;
import tools.dynamia.crud.CrudActionEvent;
import tools.dynamia.domain.query.QueryConditions;
import tools.dynamia.ui.UIMessages;

/**
 * Demo of a date range prompt: asks for a period with {@code UIMessages.showInput(LocalDateRange.class)} and filters
 * the list by publish date. The headless counterpart of {@link FilterBookByPublishDateAction}, which keeps its own
 * range widget in the ZK toolbar.
 */
@InstallAction
@RunsOn(ActionRuntime.HEADLESS)
public class FilterBookByPublishRangeAction extends AbstractCrudAction {

    public FilterBookByPublishRangeAction() {
        setName("Filter By Publish Range");
        setImage("calendar");
        setApplicableClass(Book.class);
    }

    @Override
    public void actionPerformed(CrudActionEvent evt) {
        UIMessages.showInput("Publish date range", LocalDateRange.class, range -> {
            if (range != null && !range.isNull()) {
                evt.getController().setParemeter("publishDate", QueryConditions.between(range));
                evt.getController().doQuery();
            }
        });
    }
}
