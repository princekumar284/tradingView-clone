package com.tradingapp.dto.request;

import com.tradingapp.model.Alert;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateAlertRequest {

    @NotBlank
    private String ticker;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal targetPrice;

    @NotNull
    private Alert.Condition condition;
}
