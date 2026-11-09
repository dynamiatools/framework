package mybookstore.actions;

import mybookstore.domain.Book;
import mybookstore.domain.enums.StockStatus;
import tools.dynamia.actions.Control;
import tools.dynamia.actions.ActionRuntime;
import tools.dynamia.actions.InstallAction;
import tools.dynamia.actions.RunsOn;
import tools.dynamia.crud.actions.FilterAction;

/** Filters the list by stock status with a choice; the "All" option removes the filter. */
@InstallAction
@RunsOn(ActionRuntime.FRONTEND)
public class FilterBookByStockAction extends FilterAction {

    public FilterBookByStockAction() {
        super(Book.class, Control.choice("stockStatus", StockStatus.class).allLabel("All"));
        setName("Stock");
        setAlwaysVisible(true);
    }
}
