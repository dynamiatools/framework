package mybookstore.actions;

import mybookstore.domain.Book;
import tools.dynamia.actions.InstallAction;
import tools.dynamia.crud.AbstractCrudAction;
import tools.dynamia.crud.CrudActionEvent;
import tools.dynamia.ui.UIMessages;

import java.time.LocalDate;

@InstallAction
public class FilterBookByBuyDateAction extends AbstractCrudAction {

    public FilterBookByBuyDateAction() {
        setName("Filter By Buy Date");
        setApplicableClass(Book.class);
        setImage("calendar");
        setType("primary");
        setPosition(1);
    }

    @Override
    public void actionPerformed(CrudActionEvent evt) {
        UIMessages.showInput("Select Buy Date", LocalDate.class, date -> {
            if (date != null) {
                evt.getController().setParemeter("buyDate", date); //set parameter with the selected date
                evt.getController().doQuery(); //execute query with the new parameter
            }
        });
    }
}
