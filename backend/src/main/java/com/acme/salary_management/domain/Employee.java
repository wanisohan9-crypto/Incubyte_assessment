package com.acme.salary_management.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "employees")
public class Employee {

    public enum Status {
        ACTIVE, INACTIVE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_code", nullable = false, unique = true, length = 30)
    private String employeeCode;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "country_id", nullable = false)
    private Country country;

    @Column(name = "salary_currency", nullable = false, length = 3)
    private String salaryCurrency;

    @Column(name = "annual_salary", nullable = false, precision = 15, scale = 2)
    private BigDecimal annualSalary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Status status = Status.ACTIVE;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    protected Employee() {
    }

    public Employee(String employeeCode, String firstName, String lastName, String email,
                    Country country, String salaryCurrency, BigDecimal annualSalary) {
        this.employeeCode = employeeCode;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.country = country;
        this.salaryCurrency = salaryCurrency;
        this.annualSalary = annualSalary;
    }

    @PreUpdate
    void touch() {
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.status = Status.INACTIVE;
    }

    /** Salary converted to USD using the currency rate — used for medians. */
    public BigDecimal salaryInUsd(BigDecimal usdPerUnit) {
        return annualSalary.multiply(usdPerUnit);
    }

    public Long getId() {
        return id;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public String getSalaryCurrency() {
        return salaryCurrency;
    }

    public void setSalaryCurrency(String salaryCurrency) {
        this.salaryCurrency = salaryCurrency;
    }

    public BigDecimal getAnnualSalary() {
        return annualSalary;
    }

    public void setAnnualSalary(BigDecimal annualSalary) {
        this.annualSalary = annualSalary;
    }

    public Status getStatus() {
        return status;
    }
}
