package com.tradingapp.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class FinnhubSymbolResponse {

    @JsonProperty("count")
    private int count;

    @JsonProperty("result")
    private List<FinnhubSymbol> result;

    @Data
    public static class FinnhubSymbol {

        @JsonProperty("symbol")
        private String symbol;

        @JsonProperty("description")
        private String description;

        @JsonProperty("type")
        private String type;

        @JsonProperty("primaryExchange")
        private String primaryExchange;
    }
}
