package com.acme.salary_management.web;

import com.acme.salary_management.domain.Employee;
import com.acme.salary_management.service.EmployeeService;
import com.acme.salary_management.web.dto.EmployeeRequest;
import com.acme.salary_management.web.dto.EmployeeResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService service;

    public EmployeeController(EmployeeService service) {
        this.service = service;
    }

    @GetMapping
    public Page<EmployeeResponse> list(@RequestParam(required = false) Long countryId,
                                       @RequestParam(required = false) String search,
                                       @RequestParam(required = false) Employee.Status status,
                                       @PageableDefault(size = 20) Pageable pageable) {
        return service.list(countryId, status, search, pageable);
    }

    @PostMapping
    public ResponseEntity<EmployeeResponse> create(@Valid @RequestBody EmployeeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    public EmployeeResponse update(@PathVariable Long id, @Valid @RequestBody EmployeeRequest request) {
        return service.update(id, request);
    }

    @PatchMapping("/{id}/deactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable Long id) {
        service.deactivate(id);
    }
}
