package mybookstore.actions;

import mybookstore.domain.Book;
import tools.dynamia.actions.ActionRuntime;
import tools.dynamia.actions.InstallAction;
import tools.dynamia.actions.RunsOn;
import tools.dynamia.crud.AbstractCrudAction;
import tools.dynamia.crud.CrudActionEvent;
import tools.dynamia.ui.MessageType;
import tools.dynamia.ui.UIFiles;
import tools.dynamia.ui.UIMessages;

import java.nio.charset.StandardCharsets;

/**
 * Demo of {@link UIFiles#preview}: shows a plain-text summary of the selected book in the browser (a new tab for REST
 * clients, a viewer window in ZK) instead of saving it.
 */
@InstallAction
@RunsOn(ActionRuntime.HEADLESS)
public class BookReportAction extends AbstractCrudAction {

    public BookReportAction() {
        setName("Preview Book Report");
        setImage("report");
        setApplicableClass(Book.class);
        setMenuSupported(true); // acts on one record: listed in the row menu
    }

    @Override
    public void actionPerformed(CrudActionEvent evt) {
        if (!(evt.getData() instanceof Book book)) {
            UIMessages.showMessage("Select a book first", MessageType.WARNING);
            return;
        }
        String report = "Title: " + book.getTitle() + "\nISBN: " + book.getIsbn() + "\nYear: " + book.getYear()
                + "\nPrice: " + book.getPrice() + "\nStock: " + book.getStockStatus() + "\n";
        UIFiles.preview("book-report.txt", "text/plain", report.getBytes(StandardCharsets.UTF_8));
    }
}
