package com.acme.salary_management.repository;

import com.acme.salary_management.domain.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    boolean existsByEmployeeCode(String employeeCode);

    boolean existsByEmail(String email);

    @Query("""
            SELECT e FROM Employee e
            WHERE (:countryId IS NULL OR e.country.id = :countryId)
              AND (:status IS NULL OR e.status = :status)
              AND (:search IS NULL OR LOWER(e.firstName) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(e.lastName) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(e.employeeCode) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(e.email) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Page<Employee> search(@Param("countryId") Long countryId,
                          @Param("status") Employee.Status status,
                          @Param("search") String search,
                          Pageable pageable);

    @Query("SELECT e.annualSalary, e.salaryCurrency FROM Employee e WHERE e.status = 'ACTIVE' AND (:countryId IS NULL OR e.country.id = :countryId)")
    List<Object[]> activeSalaries(@Param("countryId") Long countryId);

    long countByCountryIdAndStatus(Long countryId, Employee.Status status);

    long countByStatus(Employee.Status status);
}
