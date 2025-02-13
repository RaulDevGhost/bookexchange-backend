package com.example.bookexchange.model.dto.response;

import com.example.bookexchange.model.enums.ExchangeStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ExchangeResponse {
    private Long id;
    private UserResponse user1;
    private UserResponse user2;
    private BookResponse book1;
    private BookResponse book2;
    private ExchangeStatus status;
    private LocalDateTime proposedAt;
    private LocalDateTime meetupDateTime;
    private String meetupLocation;
    private boolean user1Confirmed;
    private boolean user2Confirmed;
}