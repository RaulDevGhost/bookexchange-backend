package com.example.bookexchange.model.entity;

import com.example.bookexchange.model.enums.UserRank;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String country;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String pictureUrl;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRank rank = UserRank.BRONZE;

    private int exchangeCount = 0;

    private Double averageRating = 0.0;

    // Relationships
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    private Set<Book> books = new HashSet<>();

    @OneToMany(mappedBy = "reviewer")
    private Set<Review> reviewsGiven = new HashSet<>();

    @OneToMany(mappedBy = "reviewed")
    private Set<Review> reviewsReceived = new HashSet<>();

    @OneToMany(mappedBy = "user1")
    private Set<Chat> chatsAsUser1 = new HashSet<>();

    @OneToMany(mappedBy = "user2")
    private Set<Chat> chatsAsUser2 = new HashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<BookMatch> matches = new HashSet<>();

    @OneToMany(mappedBy = "user1")
    private Set<Exchange> exchangesAsUser1 = new HashSet<>();

    @OneToMany(mappedBy = "user2")
    private Set<Exchange> exchangesAsUser2 = new HashSet<>();

    // Helper methods
    public Set<Chat> getAllChats() {
        Set<Chat> allChats = new HashSet<>();
        allChats.addAll(chatsAsUser1);
        allChats.addAll(chatsAsUser2);
        return allChats;
    }

    public Set<Exchange> getAllExchanges() {
        Set<Exchange> allExchanges = new HashSet<>();
        allExchanges.addAll(exchangesAsUser1);
        allExchanges.addAll(exchangesAsUser2);
        return allExchanges;
    }

    public void updateRank() {
        if (exchangeCount >= 50) {
            this.rank = UserRank.GOLD;
        } else if (exchangeCount >= 20) {
            this.rank = UserRank.SILVER;
        } else {
            this.rank = UserRank.BRONZE;
        }
    }

    public void updateAverageRating() {
        if (reviewsReceived.isEmpty()) {
            this.averageRating = 0.0;
            return;
        }

        double sum = reviewsReceived.stream()
                .mapToDouble(Review::getRating)
                .sum();
        this.averageRating = sum / reviewsReceived.size();
    }

    public void incrementExchangeCount() {
        this.exchangeCount++;
        updateRank();
    }

    // Security-related fields
    @Column(nullable = false)
    private boolean enabled = true;

    @Column(nullable = false)
    private boolean accountNonExpired = true;

    @Column(nullable = false)
    private boolean accountNonLocked = true;

    @Column(nullable = false)
    private boolean credentialsNonExpired = true;
}