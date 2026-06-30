package com.tradingapp.repository;

import com.tradingapp.model.Holding;
import com.tradingapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HoldingRepository extends JpaRepository<Holding, Long> {

    List<Holding> findByUserOrderByTickerAsc(User user);

    Optional<Holding> findByUserAndTicker(User user, String ticker);
}
