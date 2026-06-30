package com.tradingapp.controller;

import com.tradingapp.dto.response.PortfolioResponse;
import com.tradingapp.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/portfolio")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;

    @GetMapping
    public ResponseEntity<PortfolioResponse> getPortfolio(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(portfolioService.getPortfolio(userDetails.getUsername()));
    }
}
