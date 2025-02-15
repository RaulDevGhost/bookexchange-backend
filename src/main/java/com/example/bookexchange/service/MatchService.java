package com.example.bookexchange.service;

import com.example.bookexchange.model.dto.response.MatchResponse;
import com.example.bookexchange.model.entity.Book;
import com.example.bookexchange.model.entity.BookMatch;
import com.example.bookexchange.model.entity.User;
import com.example.bookexchange.repository.BookMatchRepository;
import com.example.bookexchange.repository.BookRepository;
import com.example.bookexchange.repository.UserRepository;
import com.example.bookexchange.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchService {
    private final BookMatchRepository matchRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    @Transactional
    public MatchResponse createMatch(Long userId, Long bookId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));

        // Validate book is available and not owned by the user
        if (!book.isAvailable()) {
            throw new IllegalStateException("Book is not available for exchange");
        }
        if (book.getOwner().equals(user)) {
            throw new IllegalStateException("Cannot match with your own book");
        }

        // Check if match already exists
        Optional<BookMatch> existingMatch = matchRepository
                .findByUserAndInterestedInBookAndActiveTrue(user, book);
        if (existingMatch.isPresent()) {
            throw new IllegalStateException("Match already exists");
        }

        // Create new match
        BookMatch match = new BookMatch();
        match.setUser(user);
        match.setInterestedInBook(book);

        book.setMatchCount(book.getMatchCount() + 1);
        match = matchRepository.save(match);

        return mapToMatchResponse(match);
    }

    @Transactional(readOnly = true)
    public List<MatchResponse> getUserMatches(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return matchRepository.findByUserAndActiveTrue(user).stream()
                .map(this::mapToMatchResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void cancelMatch(Long userId, Long matchId) {
        BookMatch match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found"));

        if (!match.getUser().getId().equals(userId)) {
            throw new IllegalStateException("Not authorized to cancel this match");
        }

        match.setActive(false);
        matchRepository.save(match);
    }

    private MatchResponse mapToMatchResponse(BookMatch match) {
        MatchResponse response = new MatchResponse();
        response.setId(match.getId());
        response.setUserId(match.getUser().getId());
        response.setUsername(match.getUser().getUsername());
        // Use the BookService's mapToBookResponse method in a real application
        response.setActive(match.isActive());

        // Check for reciprocal match
        Optional<BookMatch> reciprocalMatch = matchRepository.findReciprocalMatch(
                match.getInterestedInBook().getOwner(),
                match.getUser().getBooks()
        );
        response.setHasReciprocal(reciprocalMatch.isPresent());

        return response;
    }
}