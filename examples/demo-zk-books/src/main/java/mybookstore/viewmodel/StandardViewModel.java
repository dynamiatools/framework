package mybookstore.viewmodel;

import mybookstore.domain.Book;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zul.Messagebox;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.domain.util.DomainUtils;
import tools.dynamia.integration.Containers;
import tools.dynamia.ui.MessageType;
import tools.dynamia.ui.UIMessages;
import tools.dynamia.zk.crud.actions.ViewDataAction;
import tools.dynamia.zk.util.ZKBindingUtil;
import tools.dynamia.zk.util.ZKUtil;
import tools.dynamia.zk.viewers.ui.Viewer;

import java.time.Duration;
import java.util.List;

public class StandardViewModel {


    private CrudService crudService = DomainUtils.lookupCrudService(); // Lookup Patterns
    private List<Book> books;
    private Book selected;
    private List<Book> selectedBooks;

    public StandardViewModel() {
        System.out.println("Creating Standard View Model");
    }

    @Init
    public void initBooks() {
        System.out.println("Loading Standard View Model");
        books = crudService.findAll(Book.class);

        ZKUtil.runLater(Duration.ofSeconds(3), () -> UIMessages.showMessageDialog("This is a standard ZK MVVM example showing a list of books loaded from the database using Dynamia CrudService.<br/><br/>" +
                        "You can select a book and view its details or select multiple books to see how to work with multi selection in ZK MVVM.",
                "Standard ZK MVVM Example", MessageType.NORMAL));
    }

    @Command
    public void view(@BindingParam Book book) {
        book = crudService.reload(book);
        System.out.println("Viewing " + book);
        ViewDataAction viewAction = Containers.get().findObject(ViewDataAction.class);
        viewAction.view(book);
    }

    @Command
    public void delete(@BindingParam Book book) {
        UIMessages.showQuestion("Are you sure delete " + book + "?", () -> {
            UIMessages.showMessage("I cannot", MessageType.ERROR);
        });
    }

    @Command
    public void showSelected() {
        if (selected != null) {
            view(selected);
        } else {
            UIMessages.showMessage("Select book first", MessageType.ERROR);
        }
    }

    @Command
    public void clearSelected() {
        UIMessages.showQuestion("Are you sure?", () -> {
            setSelected(null);
            UIMessages.showMessage("OK");
            ZKBindingUtil.postNotifyChange(this, "selected");
        });
    }

    @Command
    public void count() {
        if (selectedBooks != null && !selectedBooks.isEmpty()) {
            Messagebox.show(selectedBooks.size() + " books selected");
            Viewer.showDialog("Selected Books", "table", Book.class, selectedBooks);
        } else {
            UIMessages.showMessage("No books selected", MessageType.WARNING);
        }
    }

    public List<Book> getBooks() {
        System.out.println("Returning books");
        return books;
    }


    public Book getSelected() {
        return selected;
    }

    public void setSelected(Book selected) {
        System.out.println("Selecting book " + selected);
        this.selected = selected;
    }

    public List<Book> getSelectedBooks() {
        return selectedBooks;
    }

    public void setSelectedBooks(List<Book> selectedBooks) {
        this.selectedBooks = selectedBooks;
    }
}
