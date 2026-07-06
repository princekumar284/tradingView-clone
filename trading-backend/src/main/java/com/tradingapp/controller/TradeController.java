package com.tradingapp.controller;

import com.tradingapp.dto.request.TradeRequest;
import com.tradingapp.dto.response.TradeResponse;
import com.tradingapp.service.TradeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trades")
@RequiredArgsConstructor
public class TradeController {

    private final TradeService tradeService;

    @PostMapping("/buy")
    public ResponseEntity<TradeResponse> buy(
            @Valid @RequestBody TradeRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(tradeService.buy(userDetails.getUsername(), request));
    }

    @PostMapping("/sell")
    public ResponseEntity<TradeResponse> sell(
            @Valid @RequestBody TradeRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(tradeService.sell(userDetails.getUsername(), request));
    }

    @GetMapping("/history")
    public ResponseEntity<List<TradeResponse>> history(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(tradeService.getTradeHistory(userDetails.getUsername()));
    }
}
