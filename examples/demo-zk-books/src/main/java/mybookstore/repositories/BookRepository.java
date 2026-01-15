package mybookstore.repositories;

import mybookstore.domain.Book;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import tools.dynamia.domain.jpa.JpaCrudServiceRepository;

import java.util.Optional;

@RepositoryRestResource(path = "books")

public interface BookRepository extends JpaRepository<Book, Long> {

    @Override
    @Cacheable(cacheNames = "books", key = "'book_'+#id")
    Optional<Book> findById(Long id);
}
