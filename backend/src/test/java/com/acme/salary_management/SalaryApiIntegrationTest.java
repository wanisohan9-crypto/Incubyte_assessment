package com.acme.salary_management;

import com.acme.salary_management.domain.Country;
import com.acme.salary_management.domain.Currency;
import com.acme.salary_management.domain.Employee;
import com.acme.salary_management.domain.User;
import com.acme.salary_management.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Full-stack test: real security filter chain, real JWT, in-memory SQLite.
 * Seeding is disabled; data is prepared per test via repositories.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "app.seed.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.datasource.url=jdbc:sqlite::memory:",
        "spring.datasource.hikari.maximum-pool-size=1"
})
class SalaryApiIntegrationTest {

    @Autowired MockMvc mockMvc;
    final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired EmployeeRepository employees;
    @Autowired CountryRepository countries;
    @Autowired CurrencyRepository currencies;
    @Autowired UserRepository users;
    @Autowired PasswordEncoder passwordEncoder;

    Country india;
    Country us;

    @BeforeEach
    void seedAndClean() {
        employees.deleteAll();
        users.deleteAll();
        countries.deleteAll();
        currencies.deleteAll();

        currencies.save(new Currency("USD", "US Dollar", new BigDecimal("1.0")));
        currencies.save(new Currency("INR", "Indian Rupee", new BigDecimal("0.012")));
        india = countries.save(new Country("India", "IN", "INR"));
        us = countries.save(new Country("United States", "US", "USD"));
        users.save(new User("hr@company.com", passwordEncoder.encode("Password123!")));
    }

    private Employee employee(String code, String email, Country c, String currency, String salary) {
        return employees.save(new Employee(code, "Rahul", "Sharma", email, c, currency, new BigDecimal(salary)));
    }

    private long employeeId(String email) {
        return employees.findAll().stream().filter(e -> e.getEmail().equals(email))
                .findFirst().orElseThrow().getId();
    }

    private String login() throws Exception {
        String body = objectMapper.writeValueAsString(
                Map.of("email", "hr@company.com", "password", "Password123!"));
        String result = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(result).get("token").asText();
    }

    private String createJson(String code, String email, long countryId, String currency, String salary) throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "employeeCode", code, "firstName", "Rahul", "lastName", "Sharma",
                "email", email, "countryId", countryId, "salaryCurrency", currency,
                "annualSalary", new BigDecimal(salary)));
    }

    @Test
    void loginWithWrongPasswordIs401() throws Exception {
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", "hr@company.com", "password", "nope"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void employeesEndpointRejectsAnonymous() throws Exception {
        mockMvc.perform(get("/api/employees")).andExpect(status().isUnauthorized());
    }

    @Test
    void crudFlowCreateReadUpdate() throws Exception {
        String token = login();

        String created = mockMvc.perform(post("/api/employees").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson("EMP001", "rahul@company.com", india.getId(), "INR", "1200000")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.countryName").value("India"))
                .andReturn().getResponse().getContentAsString();
        long id = objectMapper.readTree(created).get("id").asLong();

        mockMvc.perform(post("/api/employees").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson("EMP001", "other@company.com", india.getId(), "INR", "1")))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/api/employees").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messages").isNotEmpty());

        mockMvc.perform(put("/api/employees/" + id).header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson("EMP001", "rahul@company.com", india.getId(), "INR", "1500000")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.annualSalary").value(1500000.0));
    }

    @Test
    void searchFiltersByCountryAndDeactivationHidesFromDefaultList() throws Exception {
        String token = login();
        employee("EMP001", "a@company.com", india, "INR", "100000");
        employee("EMP002", "b@company.com", us, "USD", "50000");

        mockMvc.perform(get("/api/employees").param("countryId", String.valueOf(us.getId()))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].employeeCode").value("EMP002"));

        long indiaId = employeeId("a@company.com");
        mockMvc.perform(patch("/api/employees/" + indiaId + "/deactivate").header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/employees").param("countryId", String.valueOf(india.getId()))
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.content", hasSize(0)));

        mockMvc.perform(get("/api/employees").param("countryId", String.valueOf(india.getId()))
                        .param("status", "INACTIVE").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.content", hasSize(1)));

        mockMvc.perform(patch("/api/employees/" + indiaId + "/deactivate").header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void medianIsComputedInUsdAcrossCurrencies() throws Exception {
        String token = login();
        employee("EMP001", "a@company.com", us, "USD", "100");
        employee("EMP002", "b@company.com", us, "USD", "300");
        employee("EMP003", "c@company.com", india, "INR", "10000"); // = 120 USD

        mockMvc.perform(get("/api/salaries/median").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scope").value("GLOBAL"))
                .andExpect(jsonPath("$.medianSalaryUsd").value(120.00))
                .andExpect(jsonPath("$.employeeCount").value(3));

        mockMvc.perform(get("/api/salaries/median").param("countryId", String.valueOf(us.getId()))
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.countryName").value("United States"))
                .andExpect(jsonPath("$.medianSalaryUsd").value(200.00));

        mockMvc.perform(get("/api/salaries/median").param("countryId", "999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void countriesEndpointNeedsTokenAndListsAll() throws Exception {
        mockMvc.perform(get("/api/countries")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/countries").header("Authorization", "Bearer " + login()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void seedDisabledInTests() {
        assertThat(users.findByEmail("hr@company.com")).isPresent();
        assertThat(employees.count()).isZero();
    }
}
