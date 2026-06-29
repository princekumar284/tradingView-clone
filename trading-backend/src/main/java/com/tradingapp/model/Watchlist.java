package com.tradingapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "watchlist", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "symbol_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Watchlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "symbol_id", nullable = false)
    private Symbol symbol;

    @Column(name = "added_at", nullable = false, updatable = false)
    private Instant addedAt;

    @PrePersist
    protected void onCreate() {
        addedAt = Instant.now();
    }
}
