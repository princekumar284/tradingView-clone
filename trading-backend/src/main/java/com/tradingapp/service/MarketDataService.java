package com.tradingapp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    private static final List<String> CRYPTO_SYMBOLS = List.of(
            "btcusdt", "ethusdt", "bnbusdt", "solusdt", "xrpusdt"
    );

    @PostConstruct
    public void init() {
        connectToBinance();
    }

    private void connectToBinance() {
        try {
            String streams = String.join("/",
                    CRYPTO_SYMBOLS.stream().map(s -> s + "@ticker").toList());
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
            JsonNode data = root.path("data");

            if (data.isMissingNode()) return;

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

        } catch (Exception e) {
            log.error("Error processing Binance message: {}", e.getMessage());
        }
    }
}
