package mybookstore.actions;

import mybookstore.domain.Book;
import mybookstore.domain.enums.StockStatus;
import tools.dynamia.actions.ActionFlowContext;
import tools.dynamia.actions.ActionFlowStep;
import tools.dynamia.actions.FlowRemoteAction;
import tools.dynamia.actions.InstallAction;
import tools.dynamia.crud.AbstractCrudRemoteAction;
import tools.dynamia.domain.util.DomainUtils;
import tools.dynamia.ui.MessageType;

import java.util.Map;

/**
 * Demo {@link FlowRemoteAction}: marks a {@link Book} as out of stock, but confirms first — the flow
 * equivalent of a ZK confirm-then-mutate action, reachable from the REST/Vue frontend (theme-dynamical-vue)
 * via the generic entity actions toolbar, since it has no ZK fallback of its own.
 * <p>
 * See {@code docs/design/SERVER_DRIVEN_ACTION_FLOWS.md} — this is the Phase 3 end-to-end example.
 */
@InstallAction
public class MarkOutOfStockAction extends AbstractCrudRemoteAction implements FlowRemoteAction {

    public MarkOutOfStockAction() {
        setName("Mark Out of Stock");
        setImage("warning");
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
        return ActionFlowStep.confirm("Mark \"" + book.getTitle() + "\" as out of stock?", "Confirm");
    }

    @Override
    public ActionFlowStep resume(ActionFlowContext ctx, Object answer) {
        if (!Boolean.TRUE.equals(answer)) {
            return ActionFlowStep.done(null);
        }

        String bookId = ctx.get("bookId", String.class);
        Book book = DomainUtils.lookupCrudService().find(Book.class, bookId);
        book.setStockStatus(StockStatus.OUT_STOCK);
        Book updated = DomainUtils.lookupCrudService().update(book);

        // Return a flat projection, not the managed entity: Book has lazy JPA associations
        // (Category -> subcategories) that Jackson can't serialize once the request's Hibernate
        // session is closed (open-in-view: false) — found live while verifying this action end-to-end.
        var result = Map.of("id", updated.getId(), "title", updated.getTitle(), "stockStatus", updated.getStockStatus().name());
        return ActionFlowStep.done(result, "\"" + updated.getTitle() + "\" marked as out of stock", MessageType.INFO);
    }
}
