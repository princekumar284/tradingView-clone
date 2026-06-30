package com.tradingapp.repository;

import com.tradingapp.model.Trade;
import com.tradingapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Long> {

    List<Trade> findByUserOrderByExecutedAtDesc(User user);
}
