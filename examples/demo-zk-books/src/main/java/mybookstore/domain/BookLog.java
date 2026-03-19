package mybookstore.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import tools.dynamia.domain.Descriptor;
import tools.dynamia.domain.EntityReference;
import tools.dynamia.domain.Reference;
import tools.dynamia.domain.jpa.SimpleEntity;
import tools.dynamia.domain.util.DomainUtils;

@Entity
@Table(name = "book_logs")
@Descriptor(fields = {"id", "creationTimestamp", "bookId", "categoryId", "message"})
public class BookLog extends SimpleEntity {


    @NotNull
    @Reference("Book")
    @Descriptor(label = "Book")
    private Long bookId;

    @NotNull
    @Reference("Category")
    @Descriptor(label = "Category")
    private Long categoryId;
    private String mesesage;

    public BookLog() {
    }

    public BookLog(Long bookId, Long categoryId, String mesesage) {
        this.bookId = bookId;
        this.categoryId = categoryId;
        this.mesesage = mesesage;
    }

    public EntityReference<Long> getBookRef() {
        return DomainUtils.getEntityReference("Book", getBookId());
    }

    public EntityReference<Long> getCategoryRef() {
        return DomainUtils.getEntityReference("Category", getCategoryId());
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getMesesage() {
        return mesesage;
    }

    public void setMesesage(String mesesage) {
        this.mesesage = mesesage;
    }
}
