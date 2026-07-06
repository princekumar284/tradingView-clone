package com.tradingapp.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class PortfolioResponse {

    private BigDecimal balance;
    private List<HoldingResponse> holdings;
}
