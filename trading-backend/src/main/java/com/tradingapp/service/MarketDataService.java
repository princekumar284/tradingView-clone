package com.tradingapp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tradingapp.dto.response.CandleUpdateResponse;
import com.tradingapp.dto.response.PriceResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketDataService {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.binance.ws-url}")
    private String binanceWsUrl;

    @Value("${app.finnhub.ws-url}")
    private String finnhubWsUrl;

    @Value("${app.finnhub.api-key}")
    private String finnhubApiKey;

    private static final List<String> CRYPTO_SYMBOLS = List.of(
            "btcusdt", "ethusdt", "bnbusdt", "solusdt", "xrpusdt",
            "adausdt", "dogeusdt", "maticusdt", "dotusdt", "linkusdt",
            "avaxusdt", "uniusdt", "ltcusdt", "atomusdt", "xlmusdt"
    );

    private static final List<String> STOCK_SYMBOLS = List.of("AAPL", "GOOGL", "TSLA", "MSFT", "AMZN");

    private static final List<String> KLINE_INTERVALS = List.of("1h", "4h", "1d", "1w");

    private static final Map<String, String> BINANCE_TO_APP_INTERVAL = Map.of(
            "1h", "1H",
            "4h", "4H",
            "1d", "1D",
            "1w", "1W"
    );

    @PostConstruct
    public void init() {
        connectToBinance();
        connectToFinnhub();
    }

    private void connectToBinance() {
        try {
            List<String> tickerStreams = CRYPTO_SYMBOLS.stream()
                    .map(s -> s + "@ticker").toList();

            List<String> klineStreams = CRYPTO_SYMBOLS.stream()
                    .flatMap(s -> KLINE_INTERVALS.stream().map(i -> s + "@kline_" + i))
                    .toList();

            List<String> allStreams = new java.util.ArrayList<>();
            allStreams.addAll(tickerStreams);
            allStreams.addAll(klineStreams);

            String streams = String.join("/", allStreams);
            String url = binanceWsUrl + "/stream?streams=" + streams;

            HttpClient client = HttpClient.newHttpClient();
            client.newWebSocketBuilder()
                    .buildAsync(URI.create(url), new BinanceWebSocketListener())
                    .join();

            log.info("Connected to Binance WebSocket");
        } catch (Exception e) {
            log.error("Failed to connect to Binance WebSocket: {}", e.getMessage());
            scheduleReconnect();
        }
    }

    private void scheduleReconnect() {
        log.info("Reconnecting to Binance WebSocket in 5 seconds...");
        Executors.newSingleThreadScheduledExecutor()
                .schedule(this::connectToBinance, 5, TimeUnit.SECONDS);
    }

    private void connectToFinnhub() {
        try {
            String url = finnhubWsUrl + "?token=" + finnhubApiKey;
            HttpClient client = HttpClient.newHttpClient();
            WebSocket webSocket = client.newWebSocketBuilder()
                    .buildAsync(URI.create(url), new FinnhubWebSocketListener())
                    .join();

            for (String symbol : STOCK_SYMBOLS) {
                String msg = String.format("{\"type\":\"subscribe\",\"symbol\":\"%s\"}", symbol);
                webSocket.sendText(msg, true).join();
                log.info("Subscribed to Finnhub symbol: {}", symbol);
            }

            log.info("Connected to Finnhub WebSocket");
        } catch (Exception e) {
            log.error("Failed to connect to Finnhub WebSocket: {}", e.getMessage());
            scheduleFinnhubReconnect();
        }
    }

    private void scheduleFinnhubReconnect() {
        log.info("Reconnecting to Finnhub WebSocket in 5 seconds...");
        Executors.newSingleThreadScheduledExecutor()
                .schedule(this::connectToFinnhub, 5, TimeUnit.SECONDS);
    }

    private class FinnhubWebSocketListener implements WebSocket.Listener {

        private final StringBuilder buffer = new StringBuilder();

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            buffer.append(data);
            if (last) {
                handleFinnhubMessage(buffer.toString());
                buffer.setLength(0);
            }
            webSocket.request(1);
            return null;
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            log.error("Finnhub WebSocket error: {}", error.getMessage());
            scheduleFinnhubReconnect();
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            log.warn("Finnhub WebSocket closed: {} {}", statusCode, reason);
            scheduleFinnhubReconnect();
            return null;
        }
    }

    private void handleFinnhubMessage(String message) {
        try {
            JsonNode root = objectMapper.readTree(message);
            String type = root.path("type").asText();

            if (!"trade".equals(type)) return;

            JsonNode dataArray = root.path("data");
            if (!dataArray.isArray() || dataArray.isEmpty()) return;

            JsonNode trade = dataArray.get(dataArray.size() - 1);
            String symbol = trade.path("s").asText().toUpperCase();
            BigDecimal price = new BigDecimal(trade.path("p").asText());

            PriceResponse priceResponse = PriceResponse.builder()
                    .ticker(symbol)
                    .price(price)
                    .change(BigDecimal.ZERO)
                    .changePercent(BigDecimal.ZERO)
                    .timestamp(Instant.now())
                    .build();

            messagingTemplate.convertAndSend("/topic/price/" + symbol, priceResponse);

        } catch (Exception e) {
            log.error("Error processing Finnhub message: {}", e.getMessage());
        }
    }

    private class BinanceWebSocketListener implements WebSocket.Listener {

        private final StringBuilder buffer = new StringBuilder();

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            buffer.append(data);
            if (last) {
                handleMessage(buffer.toString());
                buffer.setLength(0);
            }
            webSocket.request(1);
            return null;
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            log.error("Binance WebSocket error: {}", error.getMessage());
            scheduleReconnect();
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            log.warn("Binance WebSocket closed: {} {}", statusCode, reason);
            scheduleReconnect();
            return null;
        }
    }

    private void handleMessage(String message) {
        try {
            JsonNode root = objectMapper.readTree(message);
            String stream = root.path("stream").asText();
            JsonNode data = root.path("data");

            if (data.isMissingNode()) return;

            if (stream.contains("@ticker")) {
                handleTickerMessage(data);
            } else if (stream.contains("@kline")) {
                handleKlineMessage(data);
            }

        } catch (Exception e) {
            log.error("Error processing Binance message: {}", e.getMessage());
        }
    }

    private void handleTickerMessage(JsonNode data) {
        String symbol = data.path("s").asText().toUpperCase();
        BigDecimal price = new BigDecimal(data.path("c").asText());
        BigDecimal change = new BigDecimal(data.path("p").asText());
        BigDecimal changePercent = new BigDecimal(data.path("P").asText());

        PriceResponse priceResponse = PriceResponse.builder()
                .ticker(symbol)
                .price(price)
                .change(change)
                .changePercent(changePercent)
                .timestamp(Instant.now())
                .build();

        messagingTemplate.convertAndSend("/topic/price/" + symbol, priceResponse);
    }

    private void handleKlineMessage(JsonNode data) {
        JsonNode k = data.path("k");
        if (k.isMissingNode()) return;

        String symbol = k.path("s").asText().toUpperCase();
        String binanceInterval = k.path("i").asText();
        String appInterval = BINANCE_TO_APP_INTERVAL.getOrDefault(binanceInterval, binanceInterval);
        long openTimeSecs = k.path("t").asLong() / 1000;

        CandleUpdateResponse candle = CandleUpdateResponse.builder()
                .ticker(symbol)
                .interval(appInterval)
                .timestamp(openTimeSecs)
                .open(new BigDecimal(k.path("o").asText()))
                .high(new BigDecimal(k.path("h").asText()))
                .low(new BigDecimal(k.path("l").asText()))
                .close(new BigDecimal(k.path("c").asText()))
                .volume(new BigDecimal(k.path("v").asText()))
                .build();

        messagingTemplate.convertAndSend("/topic/candle/" + symbol + "/" + appInterval, candle);
    }
}
