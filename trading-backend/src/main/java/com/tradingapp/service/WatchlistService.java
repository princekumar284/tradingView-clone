package com.tradingapp.service;

import com.tradingapp.dto.response.WatchlistItemResponse;
import com.tradingapp.model.Symbol;
import com.tradingapp.model.User;
import com.tradingapp.model.Watchlist;
import com.tradingapp.repository.SymbolRepository;
import com.tradingapp.repository.UserRepository;
import com.tradingapp.repository.WatchlistRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WatchlistService {

    private final WatchlistRepository watchlistRepository;
    private final UserRepository userRepository;
    private final SymbolRepository symbolRepository;

    public List<WatchlistItemResponse> getWatchlist(String email) {
        User user = getUser(email);
        return watchlistRepository.findByUserOrderByAddedAtDesc(user)
                .stream()
                .map(w -> toResponse(w))
                .toList();
    }

    public WatchlistItemResponse addToWatchlist(String email, String ticker) {
        User user = getUser(email);
        Symbol symbol = symbolRepository.findByTicker(ticker.toUpperCase())
                .orElseThrow(() -> new RuntimeException("Symbol not found: " + ticker));

        if (watchlistRepository.existsByUserAndSymbolTicker(user, ticker.toUpperCase())) {
            throw new RuntimeException(ticker + " is already in your watchlist");
        }

        Watchlist entry = Watchlist.builder()
                .user(user)
                .symbol(symbol)
                .build();

        watchlistRepository.save(entry);
        return toResponse(entry);
    }

    @Transactional
    public void removeFromWatchlist(String email, String ticker) {
        User user = getUser(email);
        if (!watchlistRepository.existsByUserAndSymbolTicker(user, ticker.toUpperCase())) {
            throw new RuntimeException(ticker + " is not in your watchlist");
        }
        watchlistRepository.deleteByUserAndSymbolTicker(user, ticker.toUpperCase());
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private WatchlistItemResponse toResponse(Watchlist w) {
        return WatchlistItemResponse.builder()
                .ticker(w.getSymbol().getTicker())
                .name(w.getSymbol().getName())
                .exchange(w.getSymbol().getExchange())
                .assetType(w.getSymbol().getAssetType())
                .addedAt(w.getAddedAt())
                .build();
    }
}
