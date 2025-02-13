package com.example.bookexchange.model.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BookResponse {
    private Long id;
    private String title;
    private String author;
    private String description;
    private String pictureUrl;
    private Long ownerId;
    private String ownerUsername;
    private int likeCount;
    private int matchCount;
    private int exchangeCount;
    private boolean available;
    private LocalDateTime createdAt;
}