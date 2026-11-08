package mybookstore.providers;

import mybookstore.domain.Book;
import org.springframework.stereotype.Component;
import tools.dynamia.domain.query.QueryConditions;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.ui.EntityLookup;

import java.util.List;
import java.util.Optional;

/** Lookup of books by title, used by the entity picker ({@code UIChoices.pickEntity("books", ...)}). */
@Component
public class BookLookup implements EntityLookup<Book> {

    private final CrudService crudService;

    public BookLookup(CrudService crudService) {
        this.crudService = crudService;
    }

    @Override
    public String name() {
        return "books";
    }

    @Override
    public boolean isSearchable() {
        return true;
    }

    @Override
    public List<Book> search(String text, int limit) {
        var params = QueryParameters.with("title", QueryConditions.like(text == null ? "" : text.trim()))
                .setMaxResults(Math.max(1, Math.min(limit, MAX_LIMIT)));
        return crudService.find(Book.class, params);
    }

    @Override
    public Optional<Book> find(String key) {
        if (key == null || key.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(crudService.find(Book.class, Long.valueOf(key)));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    @Override
    public String key(Book value) {
        return String.valueOf(value.getId());
    }

    @Override
    public String label(Book value) {
        return value.getTitle();
    }
}
