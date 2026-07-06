package com.tradingapp.service;

import com.tradingapp.dto.external.BinanceSymbolResponse;
import com.tradingapp.dto.external.FinnhubSymbolResponse;
import com.tradingapp.model.Symbol;
import com.tradingapp.repository.SymbolRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataSeeder {

    private final SymbolRepository symbolRepository;
    private final RestTemplate restTemplate;

    @Value("${app.finnhub.api-key}")
    private String finnhubApiKey;

    @Value("${app.finnhub.base-url}")
    private String finnhubBaseUrl;

    @Value("${app.binance.base-url}")
    private String binanceBaseUrl;

    private static final List<String> STOCK_TICKERS = List.of(
            "AAPL", "GOOGL", "TSLA", "MSFT", "AMZN"
    );

    private static final List<String> CRYPTO_TICKERS = List.of(
            "BTCUSDT", "ETHUSDT", "BNBUSDT", "SOLUSDT", "XRPUSDT",
            "ADAUSDT", "DOGEUSDT", "MATICUSDT", "DOTUSDT", "LINKUSDT",
            "AVAXUSDT", "UNIUSDT", "LTCUSDT", "ATOMUSDT", "XLMUSDT"
    );

    @PostConstruct
    public void init() {
        log.info("DataSeeder started...");
        seedStocks();
        seedCrypto();
        log.info("DataSeeder completed.");
    }

    private void seedStocks() {
        for (String ticker : STOCK_TICKERS) {
            if (symbolRepository.existsByTicker(ticker)) {
                log.info("Symbol {} already exists, skipping.", ticker);
                continue;
            }
            try {
                String url = finnhubBaseUrl + "/search?q=" + ticker + "&token=" + finnhubApiKey;
                FinnhubSymbolResponse response = restTemplate.getForObject(url, FinnhubSymbolResponse.class);

                if (response != null && response.getResult() != null) {
                    response.getResult().stream()
                            .filter(s -> s.getSymbol().equals(ticker))
                            .findFirst()
                            .ifPresent(s -> {
                                Symbol symbol = Symbol.builder()
                                        .ticker(s.getSymbol())
                                        .name(s.getDescription())
                                        .exchange(s.getPrimaryExchange() != null ? s.getPrimaryExchange() : "NASDAQ")
                                        .assetType(Symbol.AssetType.STOCK)
                                        .active(true)
                                        .build();
                                symbolRepository.save(symbol);
                                log.info("Saved stock symbol: {}", ticker);
                            });
                }
            } catch (Exception e) {
                log.error("Failed to seed stock symbol {}: {}", ticker, e.getMessage());
            }
        }
    }

    private void seedCrypto() {
        for (String ticker : CRYPTO_TICKERS) {
            if (symbolRepository.existsByTicker(ticker)) {
                log.info("Symbol {} already exists, skipping.", ticker);
                continue;
            }
            try {
                String url = binanceBaseUrl + "/exchangeInfo?symbol=" + ticker;
                BinanceSymbolResponse response = restTemplate.getForObject(url, BinanceSymbolResponse.class);

                if (response != null && response.getSymbols() != null) {
                    response.getSymbols().stream()
                            .filter(s -> s.getSymbol().equals(ticker) && s.getStatus().equals("TRADING"))
                            .findFirst()
                            .ifPresent(s -> {
                                Symbol symbol = Symbol.builder()
                                        .ticker(s.getSymbol())
                                        .name(s.getBaseAsset() + " / " + s.getQuoteAsset())
                                        .exchange("BINANCE")
                                        .assetType(Symbol.AssetType.CRYPTO)
                                        .active(true)
                                        .build();
                                symbolRepository.save(symbol);
                                log.info("Saved crypto symbol: {}", ticker);
                            });
                }
            } catch (Exception e) {
                log.error("Failed to seed crypto symbol {}: {}", ticker, e.getMessage());
            }
        }
    }
}
