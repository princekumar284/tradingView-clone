package com.tradingapp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tradingapp.dto.external.FinnhubNewsItem;
import com.tradingapp.dto.response.NewsArticleResponse;
import com.tradingapp.model.Symbol;
import com.tradingapp.repository.SymbolRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsService {

    private final RestTemplate restTemplate;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final SymbolRepository symbolRepository;

    @Value("${app.finnhub.api-key}")
    private String finnhubApiKey;

    @Value("${app.finnhub.base-url}")
    private String finnhubBaseUrl;

    public List<NewsArticleResponse> getNews(String ticker) {
        String cacheKey = "news:" + ticker.toUpperCase();

        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            try {
                return objectMapper.readValue(cached,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, NewsArticleResponse.class));
            } catch (JsonProcessingException e) {
                log.warn("Failed to deserialize cached news for {}", ticker);
            }
        }

        Symbol symbol = symbolRepository.findByTicker(ticker.toUpperCase()).orElse(null);
        boolean isCrypto = symbol != null && symbol.getAssetType() == Symbol.AssetType.CRYPTO;

        List<NewsArticleResponse> articles = isCrypto
                ? fetchCryptoNews()
                : fetchStockNews(ticker.toUpperCase());

        if (!articles.isEmpty()) {
            try {
                redisTemplate.opsForValue().set(cacheKey, objectMapper.writeValueAsString(articles), 15, TimeUnit.MINUTES);
            } catch (JsonProcessingException e) {
                log.warn("Failed to cache news for {}", ticker);
            }
        }

        return articles;
    }

    private List<NewsArticleResponse> fetchStockNews(String ticker) {
        try {
            String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
            String weekAgo = LocalDate.now().minusDays(7).format(DateTimeFormatter.ISO_LOCAL_DATE);

            String url = finnhubBaseUrl + "/company-news?symbol=" + ticker
                    + "&from=" + weekAgo + "&to=" + today
                    + "&token=" + finnhubApiKey;

            FinnhubNewsItem[] items = restTemplate.getForObject(url, FinnhubNewsItem[].class);
            if (items == null || items.length == 0) return Collections.emptyList();

            return Arrays.stream(items)
                    .filter(item -> item.getHeadline() != null && !item.getHeadline().isBlank())
                    .limit(10)
                    .map(this::toResponse)
                    .toList();

        } catch (Exception e) {
            log.error("Error fetching stock news for {}: {}", ticker, e.getMessage());
            return Collections.emptyList();
        }
    }

    private List<NewsArticleResponse> fetchCryptoNews() {
        try {
            String url = finnhubBaseUrl + "/news?category=crypto&token=" + finnhubApiKey;

            FinnhubNewsItem[] items = restTemplate.getForObject(url, FinnhubNewsItem[].class);
            if (items == null || items.length == 0) return Collections.emptyList();

            return Arrays.stream(items)
                    .filter(item -> item.getHeadline() != null && !item.getHeadline().isBlank())
                    .limit(10)
                    .map(this::toResponse)
                    .toList();

        } catch (Exception e) {
            log.error("Error fetching crypto news: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private NewsArticleResponse toResponse(FinnhubNewsItem item) {
        return NewsArticleResponse.builder()
                .headline(item.getHeadline())
                .source(item.getSource())
                .url(item.getUrl())
                .summary(item.getSummary())
                .image(item.getImage())
                .datetime(item.getDatetime())
                .build();
    }
}
