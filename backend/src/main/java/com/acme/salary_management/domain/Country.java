package com.acme.salary_management.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "countries")
public class Country {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "iso_code", nullable = false, unique = true, length = 2)
    private String isoCode;

    @Column(name = "default_currency_code", nullable = false, length = 3)
    private String defaultCurrencyCode;

    protected Country() {
    }

    public Country(String name, String isoCode, String defaultCurrencyCode) {
        this.name = name;
        this.isoCode = isoCode;
        this.defaultCurrencyCode = defaultCurrencyCode;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getIsoCode() {
        return isoCode;
    }

    public String getDefaultCurrencyCode() {
        return defaultCurrencyCode;
    }
}
