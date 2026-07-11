package mybookstore.repositories;

import mybookstore.domain.Book;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.Optional;

/**
 * Repository for {@link Book} entities exposed as a REST resource.
 */
@RepositoryRestResource(path = "books")
public interface BookRepository extends JpaRepository<Book, Long> {

    /**
     * Finds a book by id using a cache-backed lookup.
     *
     * @param id the book identifier
     * @return an optional containing the matching book when found
     */
    @Override
    @Cacheable(cacheNames = "books", key = "'book_'+#id")
    Optional<Book> findById(Long id);
}
