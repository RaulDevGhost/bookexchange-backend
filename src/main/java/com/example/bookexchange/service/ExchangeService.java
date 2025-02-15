package com.example.bookexchange.service;

import com.example.bookexchange.exception.BadRequestException;
import com.example.bookexchange.exception.ResourceNotFoundException;
import com.example.bookexchange.model.dto.request.ExchangeMeetupRequest;
import com.example.bookexchange.model.dto.response.ExchangeResponse;
import com.example.bookexchange.model.entity.*;
import com.example.bookexchange.model.enums.ExchangeStatus;
import com.example.bookexchange.repository.BookMatchRepository;
import com.example.bookexchange.repository.ExchangeRepository;
import com.example.bookexchange.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExchangeService {
    private final ExchangeRepository exchangeRepository;
    private final BookMatchRepository matchRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<ExchangeResponse> getUserActiveExchanges(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            
        return exchangeRepository.findActiveExchangesByUser(user).stream()
            .map(this::mapToExchangeResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ExchangeResponse> getUserExchangeHistory(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            
        return exchangeRepository.findByUserAndStatus(user, ExchangeStatus.COMPLETED).stream()
            .map(this::mapToExchangeResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public ExchangeResponse createExchange(Long userId, Long matchId) {
        BookMatch initiatingMatch = matchRepository.findById(matchId)
            .orElseThrow(() -> new ResourceNotFoundException("Match not found"));

        // Verify the user is the one who created the match
        if (!initiatingMatch.getUser().getId().equals(userId)) {
            throw new BadRequestException("Not authorized to create exchange with this match");
        }

        // Find reciprocal match
        Optional<BookMatch> reciprocalMatch = matchRepository.findReciprocalMatch(
            initiatingMatch.getInterestedInBook().getOwner(),
            initiatingMatch.getUser().getBooks()
        );

        if (reciprocalMatch.isEmpty()) {
            throw new BadRequestException("No reciprocal match found. Both users must match each other's books.");
        }

        // Create exchange
        Exchange exchange = new Exchange();
        exchange.setUser1(initiatingMatch.getUser());
        exchange.setUser2(initiatingMatch.getInterestedInBook().getOwner());
        exchange.setBook1(reciprocalMatch.get().getInterestedInBook());
        exchange.setBook2(initiatingMatch.getInterestedInBook());
        exchange.setStatus(ExchangeStatus.PROPOSED);

        // Deactivate the matches
        initiatingMatch.setActive(false);
        reciprocalMatch.get().setActive(false);
        matchRepository.save(initiatingMatch);
        matchRepository.save(reciprocalMatch.get());

        exchange = exchangeRepository.save(exchange);
        return mapToExchangeResponse(exchange);
    }

    @Transactional
    public ExchangeResponse setMeetupDetails(Long userId, ExchangeMeetupRequest request) {
        Exchange exchange = exchangeRepository.findById(request.getExchangeId())
            .orElseThrow(() -> new ResourceNotFoundException("Exchange not found"));

        // Verify user is part of the exchange
        if (!isUserPartOfExchange(userId, exchange)) {
            throw new BadRequestException("Not authorized to modify this exchange");
        }

        // Verify exchange is in PROPOSED state
        if (exchange.getStatus() != ExchangeStatus.PROPOSED) {
            throw new BadRequestException("Exchange must be in PROPOSED state to set meetup details");
        }

        // Validate meetup date is in the future
        if (request.getMeetupDateTime().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Meetup date must be in the future");
        }

        exchange.setMeetupDateTime(request.getMeetupDateTime());
        exchange.setMeetupLocation(request.getMeetupLocation());
        exchange.setStatus(ExchangeStatus.MEETUP_ARRANGED);

        exchange = exchangeRepository.save(exchange);
        return mapToExchangeResponse(exchange);
    }

    @Transactional
    public ExchangeResponse confirmExchange(Long userId, Long exchangeId) {
        Exchange exchange = exchangeRepository.findById(exchangeId)
            .orElseThrow(() -> new ResourceNotFoundException("Exchange not found"));

        // Verify user is part of the exchange
        if (!isUserPartOfExchange(userId, exchange)) {
            throw new BadRequestException("Not authorized to confirm this exchange");
        }

        // Verify exchange is in MEETUP_ARRANGED state
        if (exchange.getStatus() != ExchangeStatus.MEETUP_ARRANGED) {
            throw new BadRequestException("Exchange must be in MEETUP_ARRANGED state to confirm");
        }

        // Set confirmation based on which user is confirming
        if (exchange.getUser1().getId().equals(userId)) {
            exchange.setUser1Confirmed(true);
        } else {
            exchange.setUser2Confirmed(true);
        }

        // If both users confirmed, complete the exchange
        if (exchange.isUser1Confirmed() && exchange.isUser2Confirmed()) {
            completeExchange(exchange);
        }

        exchange = exchangeRepository.save(exchange);
        return mapToExchangeResponse(exchange);
    }

    @Transactional
    public ExchangeResponse cancelExchange(Long userId, Long exchangeId) {
        Exchange exchange = exchangeRepository.findById(exchangeId)
            .orElseThrow(() -> new ResourceNotFoundException("Exchange not found"));

        // Verify user is part of the exchange
        if (!isUserPartOfExchange(userId, exchange)) {
            throw new BadRequestException("Not authorized to cancel this exchange");
        }

        // Can only cancel if not already completed
        if (exchange.getStatus() == ExchangeStatus.COMPLETED) {
            throw new BadRequestException("Cannot cancel a completed exchange");
        }

        exchange.setStatus(ExchangeStatus.CANCELLED);
        // Reset confirmations
        exchange.setUser1Confirmed(false);
        exchange.setUser2Confirmed(false);

        exchange = exchangeRepository.save(exchange);
        return mapToExchangeResponse(exchange);
    }

    @Transactional
    public ExchangeResponse getExchangeDetails(Long userId, Long exchangeId) {
        Exchange exchange = exchangeRepository.findById(exchangeId)
            .orElseThrow(() -> new ResourceNotFoundException("Exchange not found"));

        // Verify user is part of the exchange
        if (!isUserPartOfExchange(userId, exchange)) {
            throw new BadRequestException("Not authorized to view this exchange");
        }

        return mapToExchangeResponse(exchange);
    }

    private void completeExchange(Exchange exchange) {
        exchange.setStatus(ExchangeStatus.COMPLETED);
            
        // Update exchange counts and ranks
        exchange.getUser1().incrementExchangeCount();
        exchange.getUser2().incrementExchangeCount();
        
        // Update book exchange counts and availability
        exchange.getBook1().setExchangeCount(exchange.getBook1().getExchangeCount() + 1);
        exchange.getBook2().setExchangeCount(exchange.getBook2().getExchangeCount() + 1);
        exchange.getBook1().setAvailable(false);
        exchange.getBook2().setAvailable(false);
    }

    private boolean isUserPartOfExchange(Long userId, Exchange exchange) {
        return exchange.getUser1().getId().equals(userId) || 
               exchange.getUser2().getId().equals(userId);
    }

    private ExchangeResponse mapToExchangeResponse(Exchange exchange) {
        ExchangeResponse response = new ExchangeResponse();
        response.setId(exchange.getId());

        // Set user details (still using user IDs)
        response.setUser1Id(exchange.getUser1().getId());
        response.setUser2Id(exchange.getUser2().getId());

        // Set complete book details (using mapToBookResponse method to convert to BookResponse)
        response.setBook1(mapToBookResponse(exchange.getBook1()));
        response.setBook2(mapToBookResponse(exchange.getBook2()));

        // Set exchange details
        response.setStatus(exchange.getStatus());
        response.setProposedAt(exchange.getProposedAt());
        response.setMeetupDateTime(exchange.getMeetupDateTime());
        response.setMeetupLocation(exchange.getMeetupLocation());

        // Set confirmation status
        response.setUser1Confirmed(exchange.isUser1Confirmed());
        response.setUser2Confirmed(exchange.isUser2Confirmed());

        return response;
    }

}