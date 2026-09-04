package mybookstore.actions;

import mybookstore.domain.Book;
import mybookstore.domain.BookReview;
import tools.dynamia.actions.ActionFlowContext;
import tools.dynamia.actions.ActionFlowStep;
import tools.dynamia.actions.FlowRemoteAction;
import tools.dynamia.actions.InstallAction;
import tools.dynamia.crud.AbstractCrudRemoteAction;
import tools.dynamia.domain.util.DomainUtils;
import tools.dynamia.ui.MessageType;

import java.util.HashMap;
import java.util.Map;

/**
 * Demo {@link FlowRemoteAction}: lets a user rate a {@link Book} with a bespoke star-picker widget
 * that has no equivalent among the core step types — the {@code CUSTOM} escape-hatch counterpart to
 * {@link MarkOutOfStockAction}'s {@code CONFIRM}. The "star-rating" component name is registered on
 * the client via {@code registerFlowStepRenderer} in theme-dynamical-vue only (app-specific, per
 * {@code docs/design/SERVER_DRIVEN_ACTION_FLOWS.md} §3 — {@code CUSTOM} is deliberately not a core
 * ui-core/vue component).
 * <p>
 * End-to-end example for the {@code CUSTOM} flow step renderer (#81).
 */
@InstallAction
public class RateBookAction extends AbstractCrudRemoteAction implements FlowRemoteAction {

    public RateBookAction() {
        setName("Rate Book");
        setImage("star");
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
        return ActionFlowStep.custom("star-rating", Map.of("max", 5));
    }

    @Override
    public ActionFlowStep resume(ActionFlowContext ctx, Object answer) {
        int stars;
        try {
            stars = answer instanceof Number number ? number.intValue() : Integer.parseInt(String.valueOf(answer));
        } catch (Exception e) {
            return ActionFlowStep.done(null, "Invalid rating", MessageType.ERROR);
        }
        if (stars < 1 || stars > 5) {
            return ActionFlowStep.done(null, "Invalid rating", MessageType.ERROR);
        }

        String bookId = ctx.get("bookId", String.class);
        Book book = DomainUtils.lookupCrudService().find(Book.class, bookId);

        BookReview review = new BookReview();
        review.setBook(book);
        review.setUser("Anonymous");
        review.setStars(stars);
        DomainUtils.lookupCrudService().create(review);

        var result = new HashMap<String, Object>();
        result.put("bookId", book.getId());
        result.put("stars", stars);
        return ActionFlowStep.done(result, "Thanks for rating \"" + book.getTitle() + "\"! (" + stars + "/5)", MessageType.INFO);
    }
}
