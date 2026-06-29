package com.tradingapp.repository;

import com.tradingapp.model.Alert;
import com.tradingapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    List<Alert> findByUserOrderByCreatedAtDesc(User user);

    List<Alert> findByTickerAndStatus(String ticker, Alert.Status status);
}
