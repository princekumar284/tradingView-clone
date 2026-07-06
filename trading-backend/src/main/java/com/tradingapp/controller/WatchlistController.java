package com.tradingapp.controller;

import com.tradingapp.dto.response.WatchlistItemResponse;
import com.tradingapp.service.WatchlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/watchlist")
@RequiredArgsConstructor
public class WatchlistController {

    private final WatchlistService watchlistService;

    @GetMapping
    public ResponseEntity<List<WatchlistItemResponse>> getWatchlist(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(watchlistService.getWatchlist(userDetails.getUsername()));
    }

    @PostMapping("/{ticker}")
    public ResponseEntity<WatchlistItemResponse> addToWatchlist(
            @PathVariable String ticker,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(watchlistService.addToWatchlist(userDetails.getUsername(), ticker));
    }

    @DeleteMapping("/{ticker}")
    public ResponseEntity<Void> removeFromWatchlist(
            @PathVariable String ticker,
            @AuthenticationPrincipal UserDetails userDetails) {
        watchlistService.removeFromWatchlist(userDetails.getUsername(), ticker);
        return ResponseEntity.noContent().build();
    }
}
