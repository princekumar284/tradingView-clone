package com.tradingapp.controller;

import com.tradingapp.dto.response.NewsArticleResponse;
import com.tradingapp.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class NewsController {

    private final NewsService newsService;

    @GetMapping("/{ticker}")
    public ResponseEntity<List<NewsArticleResponse>> getNews(@PathVariable String ticker) {
        return ResponseEntity.ok(newsService.getNews(ticker));
    }
}
