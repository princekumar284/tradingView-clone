package com.tradingapp.service;

import com.tradingapp.dto.request.TradeRequest;
import com.tradingapp.dto.response.TradeResponse;
import com.tradingapp.model.Holding;
import com.tradingapp.model.Trade;
import com.tradingapp.model.User;
import com.tradingapp.repository.HoldingRepository;
import com.tradingapp.repository.TradeRepository;
import com.tradingapp.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TradeService {

    private final TradeRepository tradeRepository;
    private final HoldingRepository holdingRepository;
    private final UserRepository userRepository;

    @Transactional
    public TradeResponse buy(String email, TradeRequest request) {
        User user = getUser(email);
        String ticker = request.getTicker().toUpperCase();
        BigDecimal quantity = request.getQuantity();
        BigDecimal price = request.getPrice();
        BigDecimal totalAmount = quantity.multiply(price).setScale(6, RoundingMode.HALF_UP);

        if (user.getBalance().compareTo(totalAmount) < 0) {
            throw new RuntimeException("Insufficient balance to complete this purchase");
        }

        user.setBalance(user.getBalance().subtract(totalAmount));
        userRepository.save(user);

        Holding holding = holdingRepository.findByUserAndTicker(user, ticker)
                .orElse(Holding.builder()
                        .user(user)
                        .ticker(ticker)
                        .quantity(BigDecimal.ZERO)
                        .avgBuyPrice(BigDecimal.ZERO)
                        .build());

        BigDecimal existingCost = holding.getQuantity().multiply(holding.getAvgBuyPrice());
        BigDecimal newQuantity = holding.getQuantity().add(quantity);
        BigDecimal newAvgPrice = existingCost.add(totalAmount)
                .divide(newQuantity, 6, RoundingMode.HALF_UP);

        holding.setQuantity(newQuantity);
        holding.setAvgBuyPrice(newAvgPrice);
        holdingRepository.save(holding);

        Trade trade = Trade.builder()
                .user(user)
                .ticker(ticker)
                .type(Trade.Type.BUY)
                .quantity(quantity)
                .price(price)
                .totalAmount(totalAmount)
                .build();
        tradeRepository.save(trade);

        return toResponse(trade, user.getBalance());
    }

    @Transactional
    public TradeResponse sell(String email, TradeRequest request) {
        User user = getUser(email);
        String ticker = request.getTicker().toUpperCase();
        BigDecimal quantity = request.getQuantity();
        BigDecimal price = request.getPrice();

        Holding holding = holdingRepository.findByUserAndTicker(user, ticker)
                .orElseThrow(() -> new RuntimeException("You don't own any " + ticker));

        if (holding.getQuantity().compareTo(quantity) < 0) {
            throw new RuntimeException("You only hold " + holding.getQuantity() + " " + ticker);
        }

        BigDecimal totalAmount = quantity.multiply(price).setScale(6, RoundingMode.HALF_UP);

        user.setBalance(user.getBalance().add(totalAmount));
        userRepository.save(user);

        BigDecimal remainingQuantity = holding.getQuantity().subtract(quantity);
        if (remainingQuantity.compareTo(BigDecimal.ZERO) == 0) {
            holdingRepository.delete(holding);
        } else {
            holding.setQuantity(remainingQuantity);
            holdingRepository.save(holding);
        }

        Trade trade = Trade.builder()
                .user(user)
                .ticker(ticker)
                .type(Trade.Type.SELL)
                .quantity(quantity)
                .price(price)
                .totalAmount(totalAmount)
                .build();
        tradeRepository.save(trade);

        return toResponse(trade, user.getBalance());
    }

    public List<TradeResponse> getTradeHistory(String email) {
        User user = getUser(email);
        return tradeRepository.findByUserOrderByExecutedAtDesc(user)
                .stream()
                .map(t -> toResponse(t, null))
                .toList();
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private TradeResponse toResponse(Trade trade, BigDecimal balanceAfter) {
        return TradeResponse.builder()
                .id(trade.getId())
                .ticker(trade.getTicker())
                .type(trade.getType())
                .quantity(trade.getQuantity())
                .price(trade.getPrice())
                .totalAmount(trade.getTotalAmount())
                .executedAt(trade.getExecutedAt())
                .balanceAfter(balanceAfter)
                .build();
    }
}
