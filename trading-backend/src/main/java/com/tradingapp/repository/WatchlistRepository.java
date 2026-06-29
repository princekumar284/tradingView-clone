package com.tradingapp.repository;

import com.tradingapp.model.User;
import com.tradingapp.model.Watchlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WatchlistRepository extends JpaRepository<Watchlist, Long> {

    List<Watchlist> findByUserOrderByAddedAtDesc(User user);

    boolean existsByUserAndSymbolTicker(User user, String ticker);

    void deleteByUserAndSymbolTicker(User user, String ticker);
}
