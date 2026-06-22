package com.tradingapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "symbols")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Symbol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String ticker;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private String exchange;

    @Column(name = "asset_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private AssetType assetType;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    public enum AssetType {
        STOCK, CRYPTO, FOREX
    }
}
