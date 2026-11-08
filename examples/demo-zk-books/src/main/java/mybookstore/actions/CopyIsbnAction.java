package mybookstore.actions;

import mybookstore.domain.Book;
import tools.dynamia.actions.ActionRuntime;
import tools.dynamia.actions.HeadlessCapable;
import tools.dynamia.actions.InstallAction;
import tools.dynamia.actions.RunsOn;
import tools.dynamia.crud.AbstractCrudAction;
import tools.dynamia.crud.CrudActionEvent;
import tools.dynamia.ui.MessageType;
import tools.dynamia.ui.UIClipboard;
import tools.dynamia.ui.UIMessages;

/** Demo of {@link UIClipboard#copy}: copies the ISBN of the selected book to the user's clipboard. */
@InstallAction
@RunsOn(ActionRuntime.HEADLESS)
public class CopyIsbnAction extends AbstractCrudAction implements HeadlessCapable {

    public CopyIsbnAction() {
        setName("Copy ISBN");
        setImage("copy");
        setApplicableClass(Book.class);
    }

    @Override
    public void actionPerformed(CrudActionEvent evt) {
        if (evt.getData() instanceof Book book && book.getIsbn() != null) {
            UIClipboard.copy(book.getIsbn());
            UIMessages.showMessage("ISBN copied", MessageType.INFO);
        } else {
            UIMessages.showMessage("This book has no ISBN", MessageType.WARNING);
        }
    }
}
