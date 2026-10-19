package com.acme.salary_management.web.dto;

import java.math.BigDecimal;

public record MedianResponse(
        String scope,
        Long countryId,
        String countryName,
        BigDecimal medianSalaryUsd,
        long employeeCount) {
}
