package com.tradingapp.controller;

import com.tradingapp.dto.request.CreateAlertRequest;
import com.tradingapp.dto.response.AlertResponse;
import com.tradingapp.service.AlertService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    @GetMapping
    public ResponseEntity<List<AlertResponse>> getUserAlerts(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(alertService.getUserAlerts(userDetails.getUsername()));
    }

    @PostMapping
    public ResponseEntity<AlertResponse> createAlert(
            @Valid @RequestBody CreateAlertRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(alertService.createAlert(userDetails.getUsername(), request));
    }

    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelAlert(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        alertService.cancelAlert(userDetails.getUsername(), id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAlert(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        alertService.deleteAlert(userDetails.getUsername(), id);
        return ResponseEntity.noContent().build();
    }
}
