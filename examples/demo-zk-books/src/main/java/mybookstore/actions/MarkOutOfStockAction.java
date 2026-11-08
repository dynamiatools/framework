package mybookstore.actions;

import mybookstore.domain.Book;
import mybookstore.domain.enums.StockStatus;
import tools.dynamia.actions.ActionRuntime;
import tools.dynamia.actions.HeadlessCapable;
import tools.dynamia.actions.InstallAction;
import tools.dynamia.actions.RunsOn;
import tools.dynamia.crud.AbstractCrudAction;
import tools.dynamia.crud.CrudActionEvent;
import tools.dynamia.ui.MessageType;
import tools.dynamia.ui.UIMessages;
import tools.dynamia.ui.UINavigation;

/**
 * Demo headless action: marks a {@link Book} as out of stock after asking the user. Written once against
 * {@link UIMessages}, so ZK shows a question dialog and REST/Vue clients get a CONFIRM step.
 */
@InstallAction
@RunsOn(ActionRuntime.HEADLESS)
public class MarkOutOfStockAction extends AbstractCrudAction implements HeadlessCapable {

    public MarkOutOfStockAction() {
        setName("Mark Out of Stock");
        setImage("cancel");
        setApplicableClass(Book.class);
    }

    @Override
    public void actionPerformed(CrudActionEvent evt) {
        if (!(evt.getData() instanceof Book book)) {
            UIMessages.showMessage("Select a book first", MessageType.WARNING);
            return;
        }
        UIMessages.showQuestion("Mark \"" + book.getTitle() + "\" as out of stock?", () -> {
            book.setStockStatus(StockStatus.OUT_STOCK);
            crudService().update(book);
            UIMessages.showMessage("\"" + book.getTitle() + "\" marked as out of stock", MessageType.INFO);
            UINavigation.refresh();
        });
    }
}
