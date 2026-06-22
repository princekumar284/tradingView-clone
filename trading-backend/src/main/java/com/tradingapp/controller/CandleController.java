package com.tradingapp.controller;

import com.tradingapp.model.Candle;
import com.tradingapp.service.CandleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/candles")
@RequiredArgsConstructor
public class CandleController {

    private final CandleService candleService;

    @GetMapping("/{ticker}")
    public ResponseEntity<List<Candle>> getCandles(
            @PathVariable String ticker,
            @RequestParam(defaultValue = "1D") String interval) {
        List<Candle> candles = candleService.getCandles(ticker.toUpperCase(), interval);
        return ResponseEntity.ok(candles);
    }
}
