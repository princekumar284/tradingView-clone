package com.tradingapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "holdings", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "ticker"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Holding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 20)
    private String ticker;

    @Column(nullable = false, precision = 18, scale = 8)
    private BigDecimal quantity;

    @Column(name = "avg_buy_price", nullable = false, precision = 18, scale = 6)
    private BigDecimal avgBuyPrice;
}
