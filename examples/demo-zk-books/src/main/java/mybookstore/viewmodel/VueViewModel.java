package mybookstore.viewmodel;

import mybookstore.domain.Book;
import mybookstore.repositories.BookRepository;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ToServerCommand;
import tools.dynamia.domain.util.DomainUtils;
import tools.dynamia.integration.Containers;
import tools.dynamia.ui.UIMessages;
import tools.dynamia.zk.crud.actions.ViewDataAction;

@ToServerCommand({"hello", "showBook"})
public class VueViewModel {

    //Manual lookup when you need it
    private BookRepository repository = Containers.get().findObject(BookRepository.class);

    @Command
    public void hello() {
        System.out.println("Command receive");
        UIMessages.showMessage("Hello Vue!");
    }

    @Command
    public void showBook(@BindingParam("bookId") Long bookId) {
        var book = repository.findById(bookId);
        var viewAction = Containers.get().findObject(ViewDataAction.class);
        book.ifPresent(viewAction::view);
    }
}
