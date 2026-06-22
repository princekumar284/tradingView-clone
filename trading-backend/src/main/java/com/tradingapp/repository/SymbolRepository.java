package com.tradingapp.repository;

import com.tradingapp.model.Symbol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SymbolRepository extends JpaRepository<Symbol, Long> {

    Optional<Symbol> findByTicker(String ticker);

    List<Symbol> findByActiveTrue();

    List<Symbol> findByTickerContainingIgnoreCaseOrNameContainingIgnoreCase(String ticker, String name);

    boolean existsByTicker(String ticker);
}
