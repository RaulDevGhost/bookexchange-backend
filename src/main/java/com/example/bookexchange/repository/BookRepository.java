package com.example.bookexchange.repository;

import com.example.bookexchange.model.entity.Book;
import com.example.bookexchange.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    List<Book> findByOwner(User owner);

    List<Book> findByAvailableTrue();

    // Find available books not owned by the user
    @Query("SELECT b FROM Book b WHERE b.available = true AND b.owner.id != :userId")
    List<Book> findAvailableBooksForUser(Long userId);

    // Find books by title or author containing the search term
    @Query("SELECT b FROM Book b WHERE " +
            "b.available = true AND b.owner.id != :userId AND " +
            "(LOWER(b.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(b.author) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Book> searchAvailableBooks(Long userId, String searchTerm);
}