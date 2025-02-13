package com.example.bookexchange.model.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ExchangeRequest {
    @NotNull(message = "Match ID is required")
    private Long matchId;
}