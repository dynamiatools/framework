package mybookstore.actions;

import mybookstore.domain.Book;
import tools.dynamia.actions.InstallAction;
import tools.dynamia.commons.math.Randoms;
import tools.dynamia.crud.AbstractCrudAction;
import tools.dynamia.crud.CrudActionEvent;
import tools.dynamia.domain.ValidationError;
import tools.dynamia.integration.scheduling.SchedulerUtil;
import tools.dynamia.zk.ui.LongOperationMonitorWindow;

import java.time.Duration;
import java.util.List;

@InstallAction
public class LongOperationActionDemo extends AbstractCrudAction {

    public LongOperationActionDemo() {
        setName("Run Long Operation");
        setDescription("Demo Long Operation Action");
        setApplicableClass(Book.class);
    }


    @Override
    public void actionPerformed(CrudActionEvent evt) {

        // Simulate a long operation with progress monitoring
        var win = LongOperationMonitorWindow.start("Processing all books", "Done", monitor -> {
            List<Book> books = crudService().findAll(Book.class);
            monitor.setMax(books.size());
            books.forEach(book -> {
                log("processing book: " + book.getTitle());
                monitor.setMessage("Processing book: " + book.getTitle());
                SchedulerUtil.sleep(Duration.ofSeconds(Randoms.nextInt(0, 2)));

                monitor.increment();
                if (monitor.isStopped()) {
                    monitor.setMessage("Operation cancelled by user.");
                    throw new ValidationError("Operation cancelled by user.");
                }
            });
        });
        win.setShowLog(true);


    }
}
