package com.tradingapp.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class FinnhubCandleResponse {

    @JsonProperty("o")
    private List<BigDecimal> open;

    @JsonProperty("h")
    private List<BigDecimal> high;

    @JsonProperty("l")
    private List<BigDecimal> low;

    @JsonProperty("c")
    private List<BigDecimal> close;

    @JsonProperty("v")
    private List<BigDecimal> volume;

    @JsonProperty("t")
    private List<Long> timestamp;

    @JsonProperty("s")
    private String status;
}
