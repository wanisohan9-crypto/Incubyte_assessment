package com.acme.salary_management.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "currencies")
public class Currency {

    @Id
    @Column(length = 3)
    private String code;

    @Column(nullable = false, length = 60)
    private String name;

    @Column(name = "usd_per_unit", nullable = false, precision = 18, scale = 8)
    private BigDecimal usdPerUnit;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected Currency() {
    }

    public Currency(String code, String name, BigDecimal usdPerUnit) {
        this.code = code;
        this.name = name;
        this.usdPerUnit = usdPerUnit;
        this.updatedAt = LocalDateTime.now();
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getUsdPerUnit() {
        return usdPerUnit;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
