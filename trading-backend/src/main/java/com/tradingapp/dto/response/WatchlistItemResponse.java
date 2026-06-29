package com.tradingapp.dto.response;

import com.tradingapp.model.Symbol;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class WatchlistItemResponse {

    private String ticker;
    private String name;
    private String exchange;
    private Symbol.AssetType assetType;
    private Instant addedAt;
}
