package com.tradingapp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceResponse {

    private String ticker;
    private BigDecimal price;
    private BigDecimal change;
    private BigDecimal changePercent;
    private Instant timestamp;
}
