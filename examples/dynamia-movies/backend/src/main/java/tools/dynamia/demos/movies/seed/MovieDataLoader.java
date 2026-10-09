package tools.dynamia.demos.movies.seed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.dynamia.demos.movies.domain.*;
import tools.dynamia.demos.movies.domain.enums.Certification;
import tools.dynamia.demos.movies.domain.enums.CreditRole;
import tools.dynamia.domain.services.CrudService;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

/**
 * Loads the demo catalog (about 120 movies from 1939 to 2024) from {@code data/movies.csv} the first time the
 * application starts on an empty database, plus a few synthetic reviews per movie.
 */
@Component
@Order(100)
public class MovieDataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(MovieDataLoader.class);

    private static final String[] REVIEWERS = {"Ana", "Bruno", "Carla", "Diego", "Elena", "Fabio", "Gloria", "Hugo",
            "Irene", "Javier", "Karen", "Luis", "Marta", "Nico", "Olga", "Pablo"};
    private static final String[] POSITIVE = {
            "A masterpiece. Every scene feels necessary.",
            "Rewatched it twice this month and it keeps getting better.",
            "Great performances and a story that stays with you.",
            "Beautifully shot, the soundtrack alone is worth it.",
            "One of those films that defines its genre."};
    private static final String[] MIXED = {
            "Strong first half, a little uneven afterwards.",
            "Good, though maybe overrated. Still worth watching.",
            "Memorable moments, but the pacing drags at times."};

    private final CrudService crudService;

    public MovieDataLoader(CrudService crudService) {
        this.crudService = crudService;
    }

    @Override
    public void run(String... args) throws Exception {
        if (crudService.count(Movie.class) > 0) {
            return;
        }

        var genres = new HashMap<String, Genre>();
        var people = new HashMap<String, Person>();
        var studios = new HashMap<String, Studio>();
        var random = new Random(42);
        int count = 0;

        try (var reader = new BufferedReader(new InputStreamReader(
                new ClassPathResource("data/movies.csv").getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank() || line.startsWith("#")) {
                    continue;
                }
                var c = line.split("\\|", -1);
                var movie = new Movie();
                movie.setTitle(c[0]);
                movie.setYear(Integer.parseInt(c[1]));
                movie.setRuntime(Integer.parseInt(c[2]));
                movie.setCertification(Certification.valueOf(c[3]));
                for (var g : c[4].split(";")) {
                    movie.getGenres().add(genres.computeIfAbsent(g, n -> crudService.save(new Genre(n))));
                }
                movie.addCredit(new Credit(person(people, c[5]), CreditRole.DIRECTOR, 1));
                int billing = 1;
                for (var actor : c[6].split(";")) {
                    movie.addCredit(new Credit(person(people, actor), CreditRole.ACTOR, billing++));
                }
                movie.setStudio(studios.computeIfAbsent(c[7], n -> crudService.save(new Studio(n, c[8]))));
                movie.setCountry(c[8]);
                movie.setLanguage(c[9]);
                movie.setRating(Double.parseDouble(c[10]));
                movie.setVotes((int) (Double.parseDouble(c[11]) * 1000));
                movie.setBudget(Double.parseDouble(c[12]));
                movie.setBoxOffice(Double.parseDouble(c[13]));
                movie.setSynopsis(c[14]);
                movie.setFeatured(movie.getRating() >= 8.8 || movie.getYear() >= 2023);

                int reviews = 2 + random.nextInt(3);
                for (int i = 0; i < reviews; i++) {
                    boolean good = random.nextInt(10) < 7;
                    int score = (int) Math.max(1, Math.min(10, Math.round(movie.getRating() + (good ? 0 : -1.5) + random.nextInt(2))));
                    var text = good ? POSITIVE[random.nextInt(POSITIVE.length)] : MIXED[random.nextInt(MIXED.length)];
                    var review = new Review(REVIEWERS[random.nextInt(REVIEWERS.length)], score, text,
                            LocalDate.now().minusDays(random.nextInt(900)));
                    review.setMovie(movie);
                    movie.getReviews().add(review);
                }
                crudService.save(movie);
                count++;
            }
        }
        log.info("Demo catalog loaded: {} movies, {} people, {} genres, {} studios",
                count, people.size(), genres.size(), studios.size());
    }

    private Person person(Map<String, Person> cache, String name) {
        return cache.computeIfAbsent(name, n -> crudService.save(new Person(n)));
    }
}
