package com.example.bookexchange.service;

import com.example.bookexchange.model.dto.request.BookRequest;
import com.example.bookexchange.model.dto.response.BookResponse;
import com.example.bookexchange.model.entity.Book;
import com.example.bookexchange.model.entity.User;
import com.example.bookexchange.repository.BookRepository;
import com.example.bookexchange.repository.UserRepository;
import com.example.bookexchange.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookService {
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<BookResponse> getAvailableBooks(Long userId) {
        return bookRepository.findAvailableBooksForUser(userId).stream()
                .map(this::mapToBookResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BookResponse> searchBooks(Long userId, String searchTerm) {
        return bookRepository.searchAvailableBooks(userId, searchTerm).stream()
                .map(this::mapToBookResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BookResponse getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
        return mapToBookResponse(book);
    }

    @Transactional
    public BookResponse createBook(Long userId, BookRequest request) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Book book = new Book();
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setDescription(request.getDescription());
        book.setPictureUrl(request.getPictureUrl());
        book.setOwner(owner);

        book = bookRepository.save(book);
        return mapToBookResponse(book);
    }

    @Transactional
    public BookResponse updateBook(Long bookId, BookRequest request) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));

        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setDescription(request.getDescription());
        book.setPictureUrl(request.getPictureUrl());

        book = bookRepository.save(book);
        return mapToBookResponse(book);
    }

    @Transactional
    public void toggleBookAvailability(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));

        book.setAvailable(!book.isAvailable());
        bookRepository.save(book);
    }

    private BookResponse mapToBookResponse(Book book) {
        BookResponse response = new BookResponse();
        response.setId(book.getId());
        response.setTitle(book.getTitle());
        response.setAuthor(book.getAuthor());
        response.setDescription(book.getDescription());
        response.setPictureUrl(book.getPictureUrl());
        response.setOwnerId(book.getOwner().getId());
        response.setOwnerUsername(book.getOwner().getUsername());
        response.setLikeCount(book.getLikeCount());
        response.setMatchCount(book.getMatchCount());
        response.setExchangeCount(book.getExchangeCount());
        response.setAvailable(book.isAvailable());
        return response;
    }
}