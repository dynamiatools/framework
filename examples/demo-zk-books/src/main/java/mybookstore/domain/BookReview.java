package mybookstore.domain;

import jakarta.persistence.*;
import tools.dynamia.domain.jpa.BaseEntity;


@Entity
@Table(name = "reviews")
public class BookReview extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    private Book book;
    @Column(name = "user_name")
    private String user;
    private String comment;
    private int stars;

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public int getStars() {
        return stars;
    }

    public void setStars(int stars) {
        this.stars = stars;
    }
}
