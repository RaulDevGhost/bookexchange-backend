package com.example.bookexchange.model.entity;

import com.example.bookexchange.model.enums.ExchangeStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "exchanges")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Exchange {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user1_id", nullable = false)
    private User user1;

    @ManyToOne
    @JoinColumn(name = "user2_id", nullable = false)
    private User user2;

    @ManyToOne
    @JoinColumn(name = "book1_id", nullable = false)
    private Book book1;

    @ManyToOne
    @JoinColumn(name = "book2_id", nullable = false)
    private Book book2;

    @Enumerated(EnumType.STRING)
    private ExchangeStatus status = ExchangeStatus.PROPOSED;

    private LocalDateTime proposedAt = LocalDateTime.now();
    private LocalDateTime meetupDateTime;
    private String meetupLocation;

    private boolean user1Confirmed = false;
    private boolean user2Confirmed = false;

    @OneToOne(mappedBy = "exchange")
    private Chat chat;
}