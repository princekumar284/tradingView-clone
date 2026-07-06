package com.tradingapp.service;

import com.tradingapp.dto.request.CreateAlertRequest;
import com.tradingapp.dto.response.AlertResponse;
import com.tradingapp.model.Alert;
import com.tradingapp.model.User;
import com.tradingapp.repository.AlertRepository;
import com.tradingapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepository alertRepository;
    private final UserRepository userRepository;

    public AlertResponse createAlert(String email, CreateAlertRequest request) {
        User user = getUser(email);

        Alert alert = Alert.builder()
                .user(user)
                .ticker(request.getTicker().toUpperCase())
                .targetPrice(request.getTargetPrice())
                .condition(request.getCondition())
                .status(Alert.Status.ACTIVE)
                .build();

        Alert saved = alertRepository.save(alert);
        return toResponse(saved);
    }

    public List<AlertResponse> getUserAlerts(String email) {
        User user = getUser(email);
        return alertRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public void cancelAlert(String email, Long alertId) {
        User user = getUser(email);
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found"));

        if (!alert.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Not authorized to cancel this alert");
        }

        alert.setStatus(Alert.Status.CANCELLED);
        alertRepository.save(alert);
    }

    public void deleteAlert(String email, Long alertId) {
        User user = getUser(email);
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found"));

        if (!alert.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Not authorized to delete this alert");
        }

        alertRepository.deleteById(alertId);
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public AlertResponse toResponse(Alert alert) {
        return AlertResponse.builder()
                .id(alert.getId())
                .ticker(alert.getTicker())
                .targetPrice(alert.getTargetPrice())
                .condition(alert.getCondition())
                .status(alert.getStatus())
                .createdAt(alert.getCreatedAt())
                .triggeredAt(alert.getTriggeredAt())
                .build();
    }
}
