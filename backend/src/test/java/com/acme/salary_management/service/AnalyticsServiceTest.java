package com.acme.salary_management.service;

import com.acme.salary_management.domain.Country;
import com.acme.salary_management.domain.Currency;
import com.acme.salary_management.domain.Employee;
import com.acme.salary_management.repository.CountryRepository;
import com.acme.salary_management.repository.CurrencyRepository;
import com.acme.salary_management.repository.EmployeeRepository;
import com.acme.salary_management.web.dto.MedianResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AnalyticsServiceTest {

    private final EmployeeRepository employees = mock(EmployeeRepository.class);
    private final CountryRepository countries = mock(CountryRepository.class);
    private final CurrencyRepository currencies = mock(CurrencyRepository.class);
    private final AnalyticsService service = new AnalyticsService(employees, countries, currencies);

    private void stubRates() {
        when(currencies.findAll()).thenReturn(List.of(
                new Currency("USD", "US Dollar", new BigDecimal("1.0")),
                new Currency("INR", "Indian Rupee", new BigDecimal("0.012"))));
    }

    private Object[] row(String amount, String currency) {
        return new Object[]{new BigDecimal(amount), currency};
    }

    @Test
    void globalMedianOddCountPicksMiddleValue() {
        // USD 100, INR 10000 (=120 USD), USD 300 -> sorted 100,120,300 -> median 120
        when(employees.activeSalaries(null)).thenReturn(
                List.of(row("100.00", "USD"), row("10000", "INR"), row("300.00", "USD")));
        when(employees.countByStatus(Employee.Status.ACTIVE)).thenReturn(3L);
        stubRates();

        MedianResponse r = service.median(null);

        assertThat(r.scope()).isEqualTo("GLOBAL");
        assertThat(r.medianSalaryUsd()).isEqualByComparingTo("120.00");
        assertThat(r.employeeCount()).isEqualTo(3);
    }

    @Test
    void globalMedianEvenCountAveragesMiddlePair() {
        when(employees.activeSalaries(null)).thenReturn(List.of(row("100.00", "USD"), row("300.00", "USD")));
        when(employees.countByStatus(Employee.Status.ACTIVE)).thenReturn(2L);
        stubRates();

        assertThat(service.median(null).medianSalaryUsd()).isEqualByComparingTo("200.00");
    }

    @Test
    void convertsLocalCurrencyBeforeRanking() {
        // INR 50,000 (=600 USD) must sit above USD 300 once converted
        when(employees.activeSalaries(null)).thenReturn(
                List.of(row("300.00", "USD"), row("50000", "INR")));
        when(employees.countByStatus(Employee.Status.ACTIVE)).thenReturn(2L);
        stubRates();

        assertThat(service.median(null).medianSalaryUsd()).isEqualByComparingTo("450.00");
    }

    @Test
    void countryMedianUsesCountryCountAndName() {
        Country india = new Country("India", "IN", "INR");
        when(countries.findById(1L)).thenReturn(Optional.of(india));
        when(employees.activeSalaries(1L)).thenReturn(List.of(row("10000", "INR"), row("20000", "INR")));
        when(employees.countByCountryIdAndStatus(1L, Employee.Status.ACTIVE)).thenReturn(2L);
        stubRates();

        MedianResponse r = service.median(1L);

        assertThat(r.scope()).isEqualTo("COUNTRY");
        assertThat(r.countryName()).isEqualTo("India");
        assertThat(r.medianSalaryUsd()).isEqualByComparingTo("180.00"); // (120+240)/2
        assertThat(r.employeeCount()).isEqualTo(2);
    }

    @Test
    void emptyDatasetYieldsZeroMedian() {
        when(employees.activeSalaries(null)).thenReturn(List.of());
        when(employees.countByStatus(Employee.Status.ACTIVE)).thenReturn(0L);
        stubRates();

        MedianResponse r = service.median(null);

        assertThat(r.medianSalaryUsd()).isEqualByComparingTo("0.00");
        assertThat(r.employeeCount()).isZero();
    }
}
