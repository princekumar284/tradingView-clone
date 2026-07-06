package com.tradingapp.repository;

import com.tradingapp.model.Candle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface CandleRepository extends JpaRepository<Candle, Long> {

    List<Candle> findByTickerAndIntervalOrderByTimestampAsc(String ticker, String interval);

    Optional<Candle> findByTickerAndIntervalAndTimestamp(String ticker, String interval, Instant timestamp);

    boolean existsByTickerAndIntervalAndTimestamp(String ticker, String interval, Instant timestamp);
}
