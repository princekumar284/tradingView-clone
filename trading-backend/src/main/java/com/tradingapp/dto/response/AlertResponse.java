package com.tradingapp.dto.response;

import com.tradingapp.model.Alert;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class AlertResponse {

    private Long id;
    private String ticker;
    private BigDecimal targetPrice;
    private Alert.Condition condition;
    private Alert.Status status;
    private Instant createdAt;
    private Instant triggeredAt;
}
