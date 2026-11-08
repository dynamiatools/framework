package mybookstore.actions;

import mybookstore.domain.Book;
import tools.dynamia.actions.ActionRuntime;
import tools.dynamia.actions.HeadlessCapable;
import tools.dynamia.actions.InstallAction;
import tools.dynamia.actions.RunsOn;
import tools.dynamia.crud.AbstractCrudAction;
import tools.dynamia.crud.CrudActionEvent;
import tools.dynamia.ui.MessageType;
import tools.dynamia.ui.UIMessages;
import tools.dynamia.ui.UINavigation;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Demo headless action: puts a {@link Book} on sale after asking the user for a discount percentage
 * ({@link UIMessages#showInput}, an INPUT step for remote clients).
 */
@InstallAction
@RunsOn(ActionRuntime.HEADLESS)
public class ApplyDiscountAction extends AbstractCrudAction implements HeadlessCapable {

    public ApplyDiscountAction() {
        setName("Apply Discount");
        setImage("tag");
        setApplicableClass(Book.class);
    }

    @Override
    public void actionPerformed(CrudActionEvent evt) {
        if (!(evt.getData() instanceof Book book)) {
            UIMessages.showMessage("Select a book first", MessageType.WARNING);
            return;
        }
        UIMessages.showInput("Discount percent for \"" + book.getTitle() + "\" (0-100)?", Double.class, discount -> {
            if (discount == null || discount < 0 || discount > 100) {
                UIMessages.showMessage("Invalid discount percent", MessageType.ERROR);
                return;
            }
            book.setOnSale(true);
            book.setDiscount(discount);
            if (book.getPrice() != null) {
                book.setSalePrice(book.getPrice().multiply(BigDecimal.valueOf(1 - discount / 100.0))
                        .setScale(2, RoundingMode.HALF_UP));
            }
            crudService().update(book);
            UIMessages.showMessage("\"" + book.getTitle() + "\" is now on sale at " + discount + "% off", MessageType.INFO);
            UINavigation.refresh();
        });
    }
}
