package com.example.bookexchange.repository;

import com.example.bookexchange.model.entity.Exchange;
import com.example.bookexchange.model.entity.User;
import com.example.bookexchange.model.enums.ExchangeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ExchangeRepository extends JpaRepository<Exchange, Long> {
    // Find all exchanges for a user (as either user1 or user2)
    @Query("SELECT e FROM Exchange e WHERE e.user1 = :user OR e.user2 = :user")
    List<Exchange> findAllByUser(User user);

    // Find active exchanges (PROPOSED or MEETUP_ARRANGED) for a user
    @Query("SELECT e FROM Exchange e WHERE " +
            "(e.user1 = :user OR e.user2 = :user) AND " +
            "e.status IN ('PROPOSED', 'MEETUP_ARRANGED')")
    List<Exchange> findActiveExchangesByUser(User user);

    // Find exchanges by status for a user
    @Query("SELECT e FROM Exchange e WHERE " +
            "(e.user1 = :user OR e.user2 = :user) AND " +
            "e.status = :status")
    List<Exchange> findByUserAndStatus(User user, ExchangeStatus status);
}