package com.example.bookexchange.model.dto.response;

import com.example.bookexchange.model.enums.UserRank;
import lombok.Data;
import java.util.List;

@Data
public class UserResponse {
    private Long id;
    private String username;
    private String country;
    private String city;
    private String profilePicture;
    private String description;
    private UserRank rank;
    private int exchangeCount;
    private Double averageRating;
    private List<BookResponse> books;
    private List<ReviewResponse> recentReviews;
}