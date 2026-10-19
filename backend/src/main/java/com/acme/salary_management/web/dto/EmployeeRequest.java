package com.acme.salary_management.web.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record EmployeeRequest(
        @NotBlank @Size(max = 30) String employeeCode,
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @NotBlank @Email @Size(max = 255) String email,
        @NotNull Long countryId,
        @NotBlank @Size(min = 3, max = 3) String salaryCurrency,
        @NotNull @DecimalMin(value = "0.01") BigDecimal annualSalary) {
}
