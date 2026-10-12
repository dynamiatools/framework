package tools.dynamia.demos.movies.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import tools.dynamia.domain.jpa.BaseEntity;

import java.time.LocalDate;

@Entity
@Table(name = "reviews")
public class Review extends BaseEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @NotNull
    private Movie movie;
    private String author;
    @Min(1)
    @Max(10)
    private int score = 8;
    @Column(length = 2000)
    private String comment;
    private LocalDate reviewDate = LocalDate.now();

    public Review() {
    }

    public Review(String author, int score, String comment, LocalDate reviewDate) {
        this.author = author;
        this.score = score;
        this.comment = comment;
        this.reviewDate = reviewDate;
    }

    public Movie getMovie() {
        return movie;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDate getReviewDate() {
        return reviewDate;
    }

    public void setReviewDate(LocalDate reviewDate) {
        this.reviewDate = reviewDate;
    }

    @Override
    public String toString() {
        return author + ": " + score + "/10";
    }
}
