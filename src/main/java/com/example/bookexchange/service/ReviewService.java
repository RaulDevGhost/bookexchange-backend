package com.example.bookexchange.service;

import com.example.bookexchange.exception.BadRequestException;
import com.example.bookexchange.exception.ResourceNotFoundException;
import com.example.bookexchange.model.dto.request.ReviewRequest;
import com.example.bookexchange.model.dto.response.ReviewResponse;
import com.example.bookexchange.model.entity.Exchange;
import com.example.bookexchange.model.entity.Review;
import com.example.bookexchange.model.entity.User;
import com.example.bookexchange.model.enums.ExchangeStatus;
import com.example.bookexchange.repository.ExchangeRepository;
import com.example.bookexchange.repository.ReviewRepository;
import com.example.bookexchange.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ExchangeRepository exchangeRepository;

    @Transactional
    public ReviewResponse createReview(Long reviewerId, ReviewRequest request) {
        User reviewer = userRepository.findById(reviewerId)
            .orElseThrow(() -> new ResourceNotFoundException("Reviewer not found"));

        User reviewed = userRepository.findById(request.getReviewedUserId())
            .orElseThrow(() -> new ResourceNotFoundException("Reviewed user not found"));

        // Check if these users have completed an exchange
        boolean hasCompletedExchange = exchangeRepository.findAllByUser(reviewed).stream()
            .anyMatch(exchange -> 
                exchange.getStatus() == ExchangeStatus.COMPLETED &&
                (exchange.getUser1().equals(reviewer) || exchange.getUser2().equals(reviewer))
            );

        if (!hasCompletedExchange) {
            throw new BadRequestException("Can only review users after completing an exchange with them");
        }

        // Check if review already exists
        if (reviewRepository.existsByReviewerAndReviewed(reviewer, reviewed)) {
            throw new BadRequestException("You have already reviewed this user");
        }

        Review review = new Review();
        review.setReviewer(reviewer);
        review.setReviewed(reviewed);
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        review = reviewRepository.save(review);

        // Update user's average rating
        reviewed.updateAverageRating();
        userRepository.save(reviewed);

        return mapToReviewResponse(review);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getUserReviews(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return reviewRepository.findByReviewed(user).stream()
            .map(this::mapToReviewResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public void deleteReview(Long reviewerId, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        if (!review.getReviewer().getId().equals(reviewerId)) {
            throw new BadRequestException("Not authorized to delete this review");
        }

        reviewRepository.delete(review);

        // Update user's average rating after deletion
        review.getReviewed().updateAverageRating();
        userRepository.save(review.getReviewed());
    }

    private ReviewResponse mapToReviewResponse(Review review) {
        ReviewResponse response = new ReviewResponse();
        response.setId(review.getId());
        response.setReviewerId(review.getReviewer().getId());
        response.setReviewerUsername(review.getReviewer().getUsername());
        response.setReviewedId(review.getReviewed().getId());
        response.setReviewedUsername(review.getReviewed().getUsername());
        response.setRating(review.getRating());
        response.setComment(review.getComment());
        response.setCreatedAt(review.getCreatedAt());
        return response;
    }
}