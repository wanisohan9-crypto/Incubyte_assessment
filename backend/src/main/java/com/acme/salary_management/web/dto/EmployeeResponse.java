package com.acme.salary_management.web.dto;

import java.math.BigDecimal;

public record EmployeeResponse(
        Long id,
        String employeeCode,
        String firstName,
        String lastName,
        String email,
        Long countryId,
        String countryName,
        BigDecimal annualSalary,
        String salaryCurrency,
        String status) {
}
