package com.tradingapp.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "candles",
        uniqueConstraints = @UniqueConstraint(columnNames = {"ticker", "interval_type", "timestamp"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Candle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String ticker;

    @Column(name = "interval_type", nullable = false, length = 10)
    private String interval;

    @Column(nullable = false)
    private Instant timestamp;

    @Column(nullable = false, precision = 18, scale = 6)
    private BigDecimal open;

    @Column(nullable = false, precision = 18, scale = 6)
    private BigDecimal high;

    @Column(nullable = false, precision = 18, scale = 6)
    private BigDecimal low;

    @Column(nullable = false, precision = 18, scale = 6)
    private BigDecimal close;

    @Column(nullable = false)
    private BigDecimal volume;
}
