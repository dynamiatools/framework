package mybookstore.actions;

import mybookstore.domain.Book;
import tools.dynamia.actions.ActionFlowContext;
import tools.dynamia.actions.ActionFlowStep;
import tools.dynamia.actions.FlowRemoteAction;
import tools.dynamia.actions.InstallAction;
import tools.dynamia.crud.AbstractCrudRemoteAction;
import tools.dynamia.domain.util.DomainUtils;
import tools.dynamia.ui.MessageType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * Demo {@link FlowRemoteAction}: puts a {@link Book} on sale by prompting the user for a discount
 * percentage — the {@code INPUT} step counterpart to {@link MarkOutOfStockAction}'s {@code CONFIRM},
 * reachable from the REST/Vue frontend (theme-dynamical-vue) via the generic entity actions toolbar.
 * <p>
 * See {@code docs/design/SERVER_DRIVEN_ACTION_FLOWS.md} — end-to-end example for the {@code INPUT}
 * flow step renderer (#81).
 */
@InstallAction
public class ApplyDiscountAction extends AbstractCrudRemoteAction implements FlowRemoteAction {

    public ApplyDiscountAction() {
        setName("Apply Discount");
        setImage("discount");
        setApplicableClass(Book.class);
    }

    @Override
    public ActionFlowStep start(ActionFlowContext ctx) {
        String bookId = ctx.get("dataId", String.class);
        Book book = DomainUtils.lookupCrudService().find(Book.class, bookId);
        if (book == null) {
            return ActionFlowStep.done(null, "Book not found", MessageType.ERROR);
        }

        ctx.put("bookId", bookId);
        return ActionFlowStep.input("Discount percent for \"" + book.getTitle() + "\" (0-100)?", "Apply Discount");
    }

    @Override
    public ActionFlowStep resume(ActionFlowContext ctx, Object answer) {
        double discount;
        try {
            discount = answer instanceof Number number ? number.doubleValue() : Double.parseDouble(String.valueOf(answer));
        } catch (Exception e) {
            return ActionFlowStep.done(null, "Invalid discount percent", MessageType.ERROR);
        }
        if (discount < 0 || discount > 100) {
            return ActionFlowStep.done(null, "Invalid discount percent", MessageType.ERROR);
        }

        String bookId = ctx.get("bookId", String.class);
        Book book = DomainUtils.lookupCrudService().find(Book.class, bookId);

        book.setOnSale(true);
        book.setDiscount(discount);
        if (book.getPrice() != null) {
            BigDecimal factor = BigDecimal.valueOf(1 - discount / 100.0);
            book.setSalePrice(book.getPrice().multiply(factor).setScale(2, RoundingMode.HALF_UP));
        }
        Book updated = DomainUtils.lookupCrudService().update(book);

        // Flat projection, not the managed entity — same lazy-association pitfall noted in
        // MarkOutOfStockAction.
        var result = new java.util.HashMap<String, Object>();
        result.put("id", updated.getId());
        result.put("title", updated.getTitle());
        result.put("discount", updated.getDiscount());
        result.put("salePrice", updated.getSalePrice());
        return ActionFlowStep.done(result, "\"" + updated.getTitle() + "\" is now on sale at " + discount + "% off", MessageType.INFO);
    }
}
