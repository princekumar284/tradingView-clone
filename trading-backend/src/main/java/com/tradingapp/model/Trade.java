package com.tradingapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "trades")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 20)
    private String ticker;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

    @Column(nullable = false, precision = 18, scale = 8)
    private BigDecimal quantity;

    @Column(nullable = false, precision = 18, scale = 6)
    private BigDecimal price;

    @Column(name = "total_amount", nullable = false, precision = 18, scale = 6)
    private BigDecimal totalAmount;

    @Column(name = "executed_at", nullable = false, updatable = false)
    private Instant executedAt;

    @PrePersist
    protected void onCreate() {
        executedAt = Instant.now();
    }

    public enum Type {
        BUY, SELL
    }
}
