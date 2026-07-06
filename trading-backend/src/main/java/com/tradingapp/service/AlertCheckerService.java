package com.tradingapp.service;

import com.tradingapp.dto.response.AlertResponse;
import com.tradingapp.model.Alert;
import com.tradingapp.repository.AlertRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertCheckerService {

    private final AlertRepository alertRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final AlertService alertService;

    @Transactional
    public void checkAlerts(String ticker, BigDecimal currentPrice) {
        List<Alert> activeAlerts = alertRepository.findByTickerAndStatus(ticker, Alert.Status.ACTIVE);

        for (Alert alert : activeAlerts) {
            boolean triggered = false;

            if (alert.getCondition() == Alert.Condition.ABOVE
                    && currentPrice.compareTo(alert.getTargetPrice()) >= 0) {
                triggered = true;
            } else if (alert.getCondition() == Alert.Condition.BELOW
                    && currentPrice.compareTo(alert.getTargetPrice()) <= 0) {
                triggered = true;
            }

            if (triggered) {
                alert.setStatus(Alert.Status.TRIGGERED);
                alert.setTriggeredAt(Instant.now());
                alertRepository.save(alert);

                AlertResponse response = alertService.toResponse(alert);
                Long userId = alert.getUser().getId();
                messagingTemplate.convertAndSend("/topic/alerts/" + userId, response);

                log.info("Alert triggered: {} {} {} at {}",
                        ticker, alert.getCondition(), alert.getTargetPrice(), currentPrice);
            }
        }
    }
}
