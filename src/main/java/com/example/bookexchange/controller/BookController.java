package com.example.bookexchange.controller;

import com.example.bookexchange.model.dto.request.BookRequest;
import com.example.bookexchange.model.dto.response.BookResponse;
import com.example.bookexchange.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Tag(name = "Book Controller", description = "Endpoints for managing books")
public class BookController {
    private final BookService bookService;

    @GetMapping
    @Operation(summary = "Get all available books")
    public ResponseEntity<List<BookResponse>> getAvailableBooks(Authentication authentication) {
        Long userId = getUserIdFromToken(authentication);
        return ResponseEntity.ok(bookService.getAvailableBooks(userId));
    }

    @GetMapping("/search")
    @Operation(summary = "Search books by title or author")
    public ResponseEntity<List<BookResponse>> searchBooks(
            Authentication authentication,
            @RequestParam String term
    ) {
        Long userId = getUserIdFromToken(authentication);
        return ResponseEntity.ok(bookService.searchBooks(userId, term));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get book by ID")
    public ResponseEntity<BookResponse> getBookById(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.getBookById(id));
    }

    @PostMapping
    @Operation(summary = "Add a new book")
    public ResponseEntity<BookResponse> createBook(
            Authentication authentication,
            @Valid @RequestBody BookRequest request
    ) {
        Long userId = getUserIdFromToken(authentication);
        BookResponse createdBook = bookService.createBook(userId, request);
        return new ResponseEntity<>(createdBook, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a book")
    public ResponseEntity<BookResponse> updateBook(
            @PathVariable Long id,
            @Valid @RequestBody BookRequest request
    ) {
        return ResponseEntity.ok(bookService.updateBook(id, request));
    }

    @PatchMapping("/{id}/toggle-availability")
    @Operation(summary = "Toggle book availability")
    public ResponseEntity<Void> toggleBookAvailability(@PathVariable Long id) {
        bookService.toggleBookAvailability(id);
        return ResponseEntity.noContent().build();
    }

    private Long getUserIdFromToken(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        return Long.parseLong(jwt.getClaimAsString("user_id"));
    }
}