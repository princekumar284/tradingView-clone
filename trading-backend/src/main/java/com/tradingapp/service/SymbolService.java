package com.tradingapp.service;

import com.tradingapp.model.Symbol;
import com.tradingapp.repository.SymbolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SymbolService {

    private final SymbolRepository symbolRepository;

    public List<Symbol> getAllSymbols() {
        return symbolRepository.findByActiveTrue();
    }

    public List<Symbol> searchSymbols(String query) {
        return symbolRepository.findByTickerContainingIgnoreCaseOrNameContainingIgnoreCase(query, query);
    }

    public Symbol getByTicker(String ticker) {
        return symbolRepository.findByTicker(ticker.toUpperCase())
                .orElseThrow(() -> new RuntimeException("Symbol not found: " + ticker));
    }
}
