package com.tradingapp.controller;

import com.tradingapp.model.Symbol;
import com.tradingapp.service.SymbolService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/symbols")
@RequiredArgsConstructor
public class SymbolController {

    private final SymbolService symbolService;

    @GetMapping
    public ResponseEntity<List<Symbol>> getAllSymbols() {
        return ResponseEntity.ok(symbolService.getAllSymbols());
    }

    @GetMapping("/search")
    public ResponseEntity<List<Symbol>> searchSymbols(@RequestParam String q) {
        return ResponseEntity.ok(symbolService.searchSymbols(q));
    }

    @GetMapping("/{ticker}")
    public ResponseEntity<Symbol> getByTicker(@PathVariable String ticker) {
        return ResponseEntity.ok(symbolService.getByTicker(ticker));
    }
}
