package com.acme.salary_management.service;

import com.acme.salary_management.domain.Country;
import com.acme.salary_management.domain.Currency;
import com.acme.salary_management.domain.Employee;
import com.acme.salary_management.repository.CountryRepository;
import com.acme.salary_management.repository.CurrencyRepository;
import com.acme.salary_management.repository.EmployeeRepository;
import com.acme.salary_management.web.dto.EmployeeRequest;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EmployeeServiceTest {

    private final EmployeeRepository employees = mock(EmployeeRepository.class);
    private final CountryRepository countries = mock(CountryRepository.class);
    private final CurrencyRepository currencies = mock(CurrencyRepository.class);
    private final EmployeeService service = new EmployeeService(employees, countries, currencies);

    private final Country india = new Country("India", "IN", "INR");

    private EmployeeRequest request(String code, String email) {
        return new EmployeeRequest(code, "Rahul", "Sharma", email, 1L, "INR", new BigDecimal("1200000"));
    }

    private void stubLookups() {
        when(countries.findById(1L)).thenReturn(Optional.of(india));
        when(currencies.findById("INR")).thenReturn(Optional.of(new Currency("INR", "Indian Rupee", new BigDecimal("0.012"))));
        when(employees.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void createReturnsEmployeeWithCountryName() {
        stubLookups();
        when(employees.existsByEmployeeCode("EMP001")).thenReturn(false);
        when(employees.existsByEmail("rahul@company.com")).thenReturn(false);

        var response = service.create(request("EMP001", "rahul@company.com"));

        assertThat(response.firstName()).isEqualTo("Rahul");
        assertThat(response.countryName()).isEqualTo("India");
        assertThat(response.status()).isEqualTo("ACTIVE");
    }

    @Test
    void createRejectsDuplicateEmployeeCode() {
        stubLookups();
        when(employees.existsByEmployeeCode("EMP001")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request("EMP001", "other@company.com")))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.CONFLICT);
    }

    @Test
    void createRejectsUnknownCountry() {
        when(countries.findById(99L)).thenReturn(Optional.empty());
        EmployeeRequest bad = new EmployeeRequest("EMP001", "A", "B", "a@b.com", 99L, "INR", BigDecimal.TEN);

        assertThatThrownBy(() -> service.create(bad))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.BAD_REQUEST);
    }

    @Test
    void updateRejectsEmailTakenByAnotherEmployee() {
        stubLookups();
        Employee existing = new Employee("EMP001", "Rahul", "Sharma", "rahul@company.com", india, "INR", BigDecimal.TEN);
        when(employees.findById(7L)).thenReturn(Optional.of(existing));
        when(employees.existsByEmail("taken@company.com")).thenReturn(true);

        assertThatThrownBy(() -> service.update(7L, request("EMP001", "taken@company.com")))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.CONFLICT);
    }

    @Test
    void deactivateMissingEmployeeIsNotFound() {
        when(employees.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deactivate(404L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.NOT_FOUND);
    }

    @Test
    void deactivateTwiceIsNotFound() {
        Employee existing = new Employee("EMP001", "Rahul", "Sharma", "r@c.com", india, "INR", BigDecimal.TEN);
        existing.deactivate();
        when(employees.findById(7L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.deactivate(7L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.NOT_FOUND);
    }

    @Test
    void listDefaultsToActiveWhenNoStatusGiven() {
        when(employees.search(1L, Employee.Status.ACTIVE, "rahul", PageRequest.of(0, 20)))
                .thenReturn(Page.empty(PageRequest.of(0, 20)));

        Page<?> page = service.list(1L, null, "  rahul  ", PageRequest.of(0, 20));

        assertThat(page.getTotalElements()).isZero();
    }
}
