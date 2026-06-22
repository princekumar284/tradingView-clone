package com.tradingapp.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class BinanceSymbolResponse {

    @JsonProperty("symbols")
    private List<BinanceSymbol> symbols;

    @Data
    public static class BinanceSymbol {

        @JsonProperty("symbol")
        private String symbol;

        @JsonProperty("baseAsset")
        private String baseAsset;

        @JsonProperty("quoteAsset")
        private String quoteAsset;

        @JsonProperty("status")
        private String status;
    }
}
