package com.tradingapp.dto.response;

import com.tradingapp.model.Trade;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class TradeResponse {

    private Long id;
    private String ticker;
    private Trade.Type type;
    private BigDecimal quantity;
    private BigDecimal price;
    private BigDecimal totalAmount;
    private Instant executedAt;
    private BigDecimal balanceAfter;
}
