package com.tradingapp.service;

import com.tradingapp.dto.response.HoldingResponse;
import com.tradingapp.dto.response.PortfolioResponse;
import com.tradingapp.model.Holding;
import com.tradingapp.model.User;
import com.tradingapp.repository.HoldingRepository;
import com.tradingapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final HoldingRepository holdingRepository;
    private final UserRepository userRepository;

    public PortfolioResponse getPortfolio(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        var holdings = holdingRepository.findByUserOrderByTickerAsc(user)
                .stream()
                .map(this::toResponse)
                .toList();

        return PortfolioResponse.builder()
                .balance(user.getBalance())
                .holdings(holdings)
                .build();
    }

    private HoldingResponse toResponse(Holding h) {
        return HoldingResponse.builder()
                .ticker(h.getTicker())
                .quantity(h.getQuantity())
                .avgBuyPrice(h.getAvgBuyPrice())
                .investedAmount(h.getQuantity().multiply(h.getAvgBuyPrice()))
                .build();
    }
}
