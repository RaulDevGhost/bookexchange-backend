package com.example.bookexchange.model.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReviewResponse {
    private Long id;
    private Long reviewerId;
    private String reviewerUsername;
    private Long reviewedId;
    private String reviewedUsername;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}