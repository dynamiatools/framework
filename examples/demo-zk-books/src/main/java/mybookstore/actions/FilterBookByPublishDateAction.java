package mybookstore.actions;

import mybookstore.domain.Book;
import tools.dynamia.actions.InstallAction;
import tools.dynamia.commons.LocalDateRange;
import tools.dynamia.crud.AbstractCrudAction;
import tools.dynamia.crud.CrudActionEvent;
import tools.dynamia.domain.query.QueryConditions;
import tools.dynamia.zk.actions.LocalDateboxRangeActionRenderer;

@InstallAction
public class FilterBookByPublishDateAction extends AbstractCrudAction {

    public FilterBookByPublishDateAction() {
        setRenderer(new LocalDateboxRangeActionRenderer()); //use a renderer that provides a date range input
        setApplicableClass(Book.class);
        setAlwaysVisible(true);
    }

    @Override
    public void actionPerformed(CrudActionEvent evt) {
        if(evt.getData() instanceof LocalDateRange dateRange && !dateRange.isNull()){
            evt.getController().setParemeter("publishDate", QueryConditions.between(dateRange)); //set parameter with a between condition
            evt.getController().doQuery(); //execute query with the new parameter
        }
    }
}
