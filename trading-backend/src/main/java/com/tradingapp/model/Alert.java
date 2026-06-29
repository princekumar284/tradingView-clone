package com.tradingapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "alerts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 20)
    private String ticker;

    @Column(name = "target_price", nullable = false, precision = 18, scale = 6)
    private BigDecimal targetPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Condition condition;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "triggered_at")
    private Instant triggeredAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        if (status == null) status = Status.ACTIVE;
    }

    public enum Condition {
        ABOVE, BELOW
    }

    public enum Status {
        ACTIVE, TRIGGERED, CANCELLED
    }
}
