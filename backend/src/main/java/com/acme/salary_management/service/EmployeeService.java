package com.acme.salary_management.service;

import com.acme.salary_management.domain.Country;
import com.acme.salary_management.domain.Employee;
import com.acme.salary_management.repository.CountryRepository;
import com.acme.salary_management.repository.CurrencyRepository;
import com.acme.salary_management.repository.EmployeeRepository;
import com.acme.salary_management.web.dto.EmployeeRequest;
import com.acme.salary_management.web.dto.EmployeeResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class EmployeeService {

    private final EmployeeRepository employees;
    private final CountryRepository countries;
    private final CurrencyRepository currencies;

    public EmployeeService(EmployeeRepository employees, CountryRepository countries,
                           CurrencyRepository currencies) {
        this.employees = employees;
        this.countries = countries;
        this.currencies = currencies;
    }

    @Transactional(readOnly = true)
    public Page<EmployeeResponse> list(Long countryId, Employee.Status status, String search, Pageable pageable) {
        String q = (search == null || search.isBlank()) ? null : search.trim();
        // Deactivated employees leave the default view; pass ?status=INACTIVE to audit them.
        Employee.Status effectiveStatus = (status == null) ? Employee.Status.ACTIVE : status;
        return employees.search(countryId, effectiveStatus, q, pageable).map(this::toResponse);
    }

    public EmployeeResponse create(EmployeeRequest request) {
        Country country = countries.findById(request.countryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown country"));
        currencies.findById(request.salaryCurrency())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown currency"));
        if (employees.existsByEmployeeCode(request.employeeCode())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Employee code already exists");
        }
        if (employees.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Employee email already exists");
        }
        Employee employee = new Employee(request.employeeCode(), request.firstName(), request.lastName(),
                request.email(), country, request.salaryCurrency(), request.annualSalary());
        return toResponse(employees.save(employee));
    }

    public EmployeeResponse update(Long id, EmployeeRequest request) {
        Employee employee = getActive(id);
        Country country = countries.findById(request.countryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown country"));
        currencies.findById(request.salaryCurrency())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown currency"));
        if (!employee.getEmployeeCode().equals(request.employeeCode())
                && employees.existsByEmployeeCode(request.employeeCode())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Employee code already exists");
        }
        if (!employee.getEmail().equals(request.email()) && employees.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Employee email already exists");
        }
        employee.setEmployeeCode(request.employeeCode());
        employee.setFirstName(request.firstName());
        employee.setLastName(request.lastName());
        employee.setEmail(request.email());
        employee.setCountry(country);
        employee.setSalaryCurrency(request.salaryCurrency());
        employee.setAnnualSalary(request.annualSalary());
        return toResponse(employee);
    }

    /** Soft delete: INACTIVE employees stay in history but leave listings and medians. */
    public void deactivate(Long id) {
        getActive(id).deactivate();
    }

    private Employee getActive(Long id) {
        Employee employee = employees.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
        if (employee.getStatus() != Employee.Status.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found");
        }
        return employee;
    }

    private EmployeeResponse toResponse(Employee e) {
        return new EmployeeResponse(e.getId(), e.getEmployeeCode(), e.getFirstName(), e.getLastName(),
                e.getEmail(), e.getCountry().getId(), e.getCountry().getName(),
                e.getAnnualSalary(), e.getSalaryCurrency(), e.getStatus().name());
    }
}
