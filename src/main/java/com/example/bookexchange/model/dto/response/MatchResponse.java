package com.example.bookexchange.model.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MatchResponse {
    private Long id;
    private Long userId;
    private String username;
    private BookResponse interestedInBook;
    private LocalDateTime createdAt;
    private boolean active;
    private boolean hasReciprocal; // indicates if there's a matching interest from the other user
}