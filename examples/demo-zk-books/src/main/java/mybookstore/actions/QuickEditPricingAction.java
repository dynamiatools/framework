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
import java.util.HashMap;
import java.util.Map;

/**
 * Demo {@link FlowRemoteAction}: reopens a {@link Book}'s own "form" view descriptor as a modal
 * dialog instead of a full CRUD navigation, so the user can tweak pricing without leaving the list —
 * the {@code DIALOG} step counterpart to {@link MarkOutOfStockAction}'s {@code CONFIRM}.
 * <p>
 * Prefills every field the "form" descriptor validates (not just the pricing ones) since the DIALOG
 * step re-renders the entity's real form — see {@code docs/design/SERVER_DRIVEN_ACTION_FLOWS.md} §6.
 * End-to-end example for the {@code DIALOG} flow step renderer (#81).
 */
@InstallAction
public class QuickEditPricingAction extends AbstractCrudRemoteAction implements FlowRemoteAction {

    public QuickEditPricingAction() {
        setName("Quick Edit Pricing");
        setImage("edit");
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

        // HashMap, not Map.of(): most of these fields are legitimately nullable on Book, and
        // Map.of() throws NullPointerException on its first null value.
        var data = new HashMap<String, Object>();
        data.put("id", book.getId());
        data.put("title", book.getTitle());
        data.put("sinopsys", book.getSinopsys());
        data.put("year", book.getYear());
        data.put("isbn", book.getIsbn());
        data.put("category", book.getCategory() != null ? book.getCategory().getId() : null);
        data.put("publishDate", book.getPublishDate());
        data.put("buyDate", book.getBuyDate());
        data.put("price", book.getPrice());
        data.put("stockStatus", book.getStockStatus());
        data.put("onSale", book.isOnSale());
        data.put("salePrice", book.getSalePrice());
        data.put("discount", book.getDiscount());

        return ActionFlowStep.dialog("form", data, "Quick edit pricing");
    }

    @Override
    @SuppressWarnings("unchecked")
    public ActionFlowStep resume(ActionFlowContext ctx, Object answer) {
        if (!(answer instanceof Map)) {
            return ActionFlowStep.done(null, "Invalid form submission", MessageType.ERROR);
        }
        Map<String, Object> form = (Map<String, Object>) answer;

        String bookId = ctx.get("bookId", String.class);
        Book book = DomainUtils.lookupCrudService().find(Book.class, bookId);

        toBigDecimal(form.get("price")).ifPresent(book::setPrice);
        toBigDecimal(form.get("salePrice")).ifPresent(book::setSalePrice);
        if (form.containsKey("onSale")) {
            book.setOnSale(Boolean.TRUE.equals(form.get("onSale")));
        }
        toDouble(form.get("discount")).ifPresent(book::setDiscount);

        Book updated = DomainUtils.lookupCrudService().update(book);

        var result = new HashMap<String, Object>();
        result.put("id", updated.getId());
        result.put("title", updated.getTitle());
        result.put("price", updated.getPrice());
        result.put("onSale", updated.isOnSale());
        result.put("salePrice", updated.getSalePrice());
        result.put("discount", updated.getDiscount());
        return ActionFlowStep.done(result, "Pricing updated for \"" + updated.getTitle() + "\"", MessageType.INFO);
    }

    private static java.util.Optional<BigDecimal> toBigDecimal(Object value) {
        if (value instanceof Number number) {
            return java.util.Optional.of(BigDecimal.valueOf(number.doubleValue()));
        }
        if (value instanceof String s && !s.isBlank()) {
            try {
                return java.util.Optional.of(new BigDecimal(s));
            } catch (NumberFormatException ignored) {
                // fall through
            }
        }
        return java.util.Optional.empty();
    }

    private static java.util.Optional<Double> toDouble(Object value) {
        if (value instanceof Number number) {
            return java.util.Optional.of(number.doubleValue());
        }
        if (value instanceof String s && !s.isBlank()) {
            try {
                return java.util.Optional.of(Double.parseDouble(s));
            } catch (NumberFormatException ignored) {
                // fall through
            }
        }
        return java.util.Optional.empty();
    }
}
