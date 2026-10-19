package com.acme.salary_management.service;

import com.acme.salary_management.domain.Employee;
import com.acme.salary_management.repository.CountryRepository;
import com.acme.salary_management.repository.CurrencyRepository;
import com.acme.salary_management.repository.EmployeeRepository;
import com.acme.salary_management.web.dto.MedianResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    private final EmployeeRepository employees;
    private final CountryRepository countries;
    private final CurrencyRepository currencies;

    public AnalyticsService(EmployeeRepository employees, CountryRepository countries,
                            CurrencyRepository currencies) {
        this.employees = employees;
        this.countries = countries;
        this.currencies = currencies;
    }

    /**
     * Median in USD over ACTIVE employees. SQLite has no MEDIAN() aggregate,
     * so salaries are converted to USD and the median is picked from the
     * sorted list — O(n log n), fine for 10k rows.
     */
    public MedianResponse median(Long countryId) {
        String countryName = null;
        if (countryId != null) {
            var country = countries.findById(countryId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown country"));
            countryName = country.getName();
        }
        var rates = new java.util.HashMap<String, BigDecimal>();
        currencies.findAll().forEach(c -> rates.put(c.getCode(), c.getUsdPerUnit()));

        List<BigDecimal> usd = new ArrayList<>();
        for (Object[] row : employees.activeSalaries(countryId)) {
            BigDecimal amount = (BigDecimal) row[0];
            BigDecimal rate = rates.get((String) row[1]);
            if (amount != null && rate != null) {
                usd.add(amount.multiply(rate));
            }
        }
        Collections.sort(usd);
        if (usd.isEmpty()) {
            return new MedianResponse(countryId == null ? "GLOBAL" : "COUNTRY", countryId,
                    countryName, BigDecimal.ZERO.setScale(2), 0);
        }
        int n = usd.size();
        BigDecimal median = (n % 2 == 1)
                ? usd.get(n / 2)
                : usd.get(n / 2 - 1).add(usd.get(n / 2))
                        .divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
        long count = countryId == null
                ? employees.countByStatus(Employee.Status.ACTIVE)
                : employees.countByCountryIdAndStatus(countryId, Employee.Status.ACTIVE);
        return new MedianResponse(countryId == null ? "GLOBAL" : "COUNTRY", countryId,
                countryName, median.setScale(2, RoundingMode.HALF_UP), count);
    }
}
