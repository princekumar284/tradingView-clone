package com.tradingapp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tradingapp.dto.external.AlphaVantageCandleResponse;
import com.tradingapp.model.Candle;
import com.tradingapp.model.Symbol;
import com.tradingapp.repository.CandleRepository;
import com.tradingapp.repository.SymbolRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class CandleService {

    private final CandleRepository candleRepository;
    private final SymbolRepository symbolRepository;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Value("${app.alpha-vantage.api-key}")
    private String alphaVantageApiKey;

    @Value("${app.alpha-vantage.base-url}")
    private String alphaVantageBaseUrl;

    @Value("${app.binance.base-url}")
    private String binanceBaseUrl;

    public List<Candle> getCandles(String ticker, String interval) {
        String cacheKey = "candles:" + ticker + ":" + interval;

        // Step 1: Check Redis
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.info("Cache HIT for {}", cacheKey);
            try {
                return objectMapper.readValue(cached,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, Candle.class));
            } catch (JsonProcessingException e) {
                log.warn("Failed to deserialize cached candles for {}", cacheKey);
            }
        }

        // Step 2: Check PostgreSQL
        List<Candle> candles = candleRepository.findByTickerAndIntervalOrderByTimestampAsc(ticker, interval);
        if (!candles.isEmpty()) {
            log.info("DB HIT for {}", cacheKey);
            putInCache(cacheKey, candles);
            return candles;
        }

        // Step 3: Fetch from external API based on asset type
        log.info("Fetching from external API for {}", cacheKey);
        Symbol symbol = symbolRepository.findByTicker(ticker).orElse(null);

        if (symbol != null && symbol.getAssetType() == Symbol.AssetType.CRYPTO) {
            candles = fetchFromBinance(ticker, interval);
        } else {
            candles = fetchFromAlphaVantage(ticker);
        }

        if (!candles.isEmpty()) {
            candleRepository.saveAll(candles);
            putInCache(cacheKey, candles);
        }

        return candles;
    }

    private List<Candle> fetchFromAlphaVantage(String ticker) {
        try {
            String url = alphaVantageBaseUrl + "/query?function=TIME_SERIES_DAILY&symbol=" + ticker
                    + "&outputsize=compact&apikey=" + alphaVantageApiKey;

            AlphaVantageCandleResponse response = restTemplate.getForObject(url, AlphaVantageCandleResponse.class);

            if (response == null || response.getTimeSeries() == null) {
                log.warn("No candle data from Alpha Vantage for {}", ticker);
                return Collections.emptyList();
            }

            List<Candle> candles = new ArrayList<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            for (Map.Entry<String, AlphaVantageCandleResponse.DailyData> entry : response.getTimeSeries().entrySet()) {
                LocalDate date = LocalDate.parse(entry.getKey(), formatter);
                AlphaVantageCandleResponse.DailyData data = entry.getValue();

                Candle candle = Candle.builder()
                        .ticker(ticker)
                        .interval("1D")
                        .timestamp(date.atStartOfDay().toInstant(ZoneOffset.UTC))
                        .open(new BigDecimal(data.getOpen()))
                        .high(new BigDecimal(data.getHigh()))
                        .low(new BigDecimal(data.getLow()))
                        .close(new BigDecimal(data.getClose()))
                        .volume(new BigDecimal(data.getVolume()))
                        .build();
                candles.add(candle);
            }

            candles.sort(Comparator.comparing(Candle::getTimestamp));
            log.info("Fetched {} candles from Alpha Vantage for {}", candles.size(), ticker);
            return candles;

        } catch (Exception e) {
            log.error("Error fetching candles from Alpha Vantage for {}: {}", ticker, e.getMessage());
            return Collections.emptyList();
        }
    }

    private List<Candle> fetchFromBinance(String ticker, String interval) {
        try {
            String binanceInterval = toBinanceInterval(interval);
            String url = binanceBaseUrl + "/klines?symbol=" + ticker
                    + "&interval=" + binanceInterval + "&limit=100";

            List<List<Object>> response = restTemplate.getForObject(url, List.class);

            if (response == null || response.isEmpty()) {
                log.warn("No candle data from Binance for {}", ticker);
                return Collections.emptyList();
            }

            List<Candle> candles = new ArrayList<>();
            for (List<Object> kline : response) {
                long openTime = ((Number) kline.get(0)).longValue();
                Candle candle = Candle.builder()
                        .ticker(ticker)
                        .interval(interval)
                        .timestamp(Instant.ofEpochMilli(openTime))
                        .open(new BigDecimal(kline.get(1).toString()))
                        .high(new BigDecimal(kline.get(2).toString()))
                        .low(new BigDecimal(kline.get(3).toString()))
                        .close(new BigDecimal(kline.get(4).toString()))
                        .volume(new BigDecimal(kline.get(5).toString()))
                        .build();
                candles.add(candle);
            }

            log.info("Fetched {} candles from Binance for {}", candles.size(), ticker);
            return candles;

        } catch (Exception e) {
            log.error("Error fetching candles from Binance for {}: {}", ticker, e.getMessage());
            return Collections.emptyList();
        }
    }

    private void putInCache(String cacheKey, List<Candle> candles) {
        try {
            String json = objectMapper.writeValueAsString(candles);
            redisTemplate.opsForValue().set(cacheKey, json, 5, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            log.warn("Failed to cache candles for {}", cacheKey);
        }
    }

    private String toBinanceInterval(String interval) {
        return switch (interval) {
            case "1min" -> "1m";
            case "5min" -> "5m";
            case "15min" -> "15m";
            case "30min" -> "30m";
            case "1H" -> "1h";
            case "4H" -> "4h";
            case "1D" -> "1d";
            case "1W" -> "1w";
            default -> "1d";
        };
    }
}
