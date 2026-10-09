package tools.dynamia.demos.movies.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import tools.dynamia.demos.movies.domain.enums.Certification;
import tools.dynamia.domain.InitializeOnLoad;
import tools.dynamia.domain.OrderBy;
import tools.dynamia.domain.jpa.BaseEntity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "movies", indexes = {@Index(columnList = "title"), @Index(columnList = "release_year")})
@OrderBy("title")
public class Movie extends BaseEntity {

    @NotEmpty
    private String title;
    @Column(name = "release_year")
    private int year;
    private Integer runtime;
    @Enumerated(EnumType.STRING)
    private Certification certification = Certification.NR;
    private String language;
    private String country;
    @Column(length = 2000)
    private String synopsis;
    private String posterUrl;
    private String trailerUrl;

    /** Average score, 0-10. */
    private double rating;
    private int votes;
    /** In millions of USD. */
    private Double budget;
    private Double boxOffice;
    private boolean featured;

    @ManyToOne
    private Studio studio;

    @ManyToMany
    @JoinTable(name = "movies_genres")
    @InitializeOnLoad
    private Set<Genre> genres = new HashSet<>();

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    @InitializeOnLoad
    private List<Credit> credits = new ArrayList<>();

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();

    // Denormalized columns: they let the automatic REST API filter and show a movie without joins.
    private String genresText;
    private String directorName;
    @Column(length = 500)
    private String topCast;
    @Column(length = 4000)
    private String searchText;

    @PrePersist
    @PreUpdate
    void updateDenormalizedColumns() {
        genresText = genres.stream().map(Genre::getName).sorted().collect(Collectors.joining(", "));
        directorName = credits.stream().filter(c -> c.getRole() == tools.dynamia.demos.movies.domain.enums.CreditRole.DIRECTOR)
                .map(c -> c.getPerson().getName()).collect(Collectors.joining(", "));
        topCast = credits.stream().filter(c -> c.getRole() == tools.dynamia.demos.movies.domain.enums.CreditRole.ACTOR)
                .sorted(java.util.Comparator.comparingInt(Credit::getBilling))
                .limit(4).map(c -> c.getPerson().getName()).collect(Collectors.joining(", "));
        var text = String.join(" ", title, directorName, topCast, genresText, String.valueOf(year),
                studio != null ? studio.getName() : "", country != null ? country : "");
        searchText = text.toLowerCase(Locale.ROOT);
    }

    public Movie addCredit(Credit credit) {
        credit.setMovie(this);
        credits.add(credit);
        return this;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public Integer getRuntime() {
        return runtime;
    }

    public void setRuntime(Integer runtime) {
        this.runtime = runtime;
    }

    public Certification getCertification() {
        return certification;
    }

    public void setCertification(Certification certification) {
        this.certification = certification;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getSynopsis() {
        return synopsis;
    }

    public void setSynopsis(String synopsis) {
        this.synopsis = synopsis;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public String getTrailerUrl() {
        return trailerUrl;
    }

    public void setTrailerUrl(String trailerUrl) {
        this.trailerUrl = trailerUrl;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getVotes() {
        return votes;
    }

    public void setVotes(int votes) {
        this.votes = votes;
    }

    public Double getBudget() {
        return budget;
    }

    public void setBudget(Double budget) {
        this.budget = budget;
    }

    public Double getBoxOffice() {
        return boxOffice;
    }

    public void setBoxOffice(Double boxOffice) {
        this.boxOffice = boxOffice;
    }

    public boolean isFeatured() {
        return featured;
    }

    public void setFeatured(boolean featured) {
        this.featured = featured;
    }

    public Studio getStudio() {
        return studio;
    }

    public void setStudio(Studio studio) {
        this.studio = studio;
    }

    public Set<Genre> getGenres() {
        return genres;
    }

    public void setGenres(Set<Genre> genres) {
        this.genres = genres;
    }

    public List<Credit> getCredits() {
        return credits;
    }

    public void setCredits(List<Credit> credits) {
        this.credits = credits;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    public String getGenresText() {
        return genresText;
    }

    public String getDirectorName() {
        return directorName;
    }

    public String getTopCast() {
        return topCast;
    }

    public String getSearchText() {
        return searchText;
    }

    @Override
    public String toString() {
        return title + " (" + year + ")";
    }
}
