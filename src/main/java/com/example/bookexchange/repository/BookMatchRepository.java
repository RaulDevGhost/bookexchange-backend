package com.example.bookexchange.repository;

import com.example.bookexchange.model.entity.Book;
import com.example.bookexchange.model.entity.BookMatch;
import com.example.bookexchange.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface BookMatchRepository extends JpaRepository<BookMatch, Long> {
    List<BookMatch> findByUserAndActiveTrue(User user);

    // Find active match between user and book
    Optional<BookMatch> findByUserAndInterestedInBookAndActiveTrue(User user, Book book);

    // Find reciprocal match (if user B matched with user A's book)
    @Query("SELECT m FROM BookMatch m " +
            "WHERE m.active = true " +
            "AND m.user = :bookOwner " +
            "AND m.interestedInBook IN :userBooks")
    Optional<BookMatch> findReciprocalMatch(User bookOwner, Set<Book> userBooks);

    // Find all active matches for a book
    List<BookMatch> findByInterestedInBookAndActiveTrue(Book book);
}