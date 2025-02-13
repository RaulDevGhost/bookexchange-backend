package com.example.bookexchange.model.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MatchRequest {
    @NotNull(message = "Book ID is required")
    private Long bookId;
}