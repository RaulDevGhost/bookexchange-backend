package com.example.bookexchange.controller;

import com.example.bookexchange.model.dto.request.MatchRequest;
import com.example.bookexchange.model.dto.response.MatchResponse;
import com.example.bookexchange.service.MatchService;
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
@RequestMapping("/api/matches")
@RequiredArgsConstructor
@Tag(name = "Match Controller", description = "Endpoints for managing book matches")
public class MatchController {
    private final MatchService matchService;

    @PostMapping
    @Operation(summary = "Create a new match with a book")
    public ResponseEntity<MatchResponse> createMatch(
            Authentication authentication,
            @Valid @RequestBody MatchRequest request
    ) {
        Long userId = getUserIdFromToken(authentication);
        MatchResponse match = matchService.createMatch(userId, request.getBookId());
        return new ResponseEntity<>(match, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all active matches for the current user")
    public ResponseEntity<List<MatchResponse>> getUserMatches(Authentication authentication) {
        Long userId = getUserIdFromToken(authentication);
        return ResponseEntity.ok(matchService.getUserMatches(userId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel a match")
    public ResponseEntity<Void> cancelMatch(
            Authentication authentication,
            @PathVariable Long id
    ) {
        Long userId = getUserIdFromToken(authentication);
        matchService.cancelMatch(userId, id);
        return ResponseEntity.noContent().build();
    }

    private Long getUserIdFromToken(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        return Long.parseLong(jwt.getClaimAsString("user_id"));
    }
}