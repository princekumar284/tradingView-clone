package com.tradingapp.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class HoldingResponse {

    private String ticker;
    private BigDecimal quantity;
    private BigDecimal avgBuyPrice;
    private BigDecimal investedAmount;
}
