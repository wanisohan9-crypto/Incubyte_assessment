package com.acme.salary_management.seed;

import com.acme.salary_management.domain.Country;
import com.acme.salary_management.domain.Currency;
import com.acme.salary_management.domain.Employee;
import com.acme.salary_management.domain.User;
import com.acme.salary_management.repository.CountryRepository;
import com.acme.salary_management.repository.CurrencyRepository;
import com.acme.salary_management.repository.EmployeeRepository;
import com.acme.salary_management.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class SeedRunner implements ApplicationRunner {

    private final CountryRepository countries;
    private final CurrencyRepository currencies;
    private final EmployeeRepository employees;
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final boolean enabled;
    private final int employeeCount;
    private final String hrEmail;
    private final String hrPassword;

    public SeedRunner(CountryRepository countries, CurrencyRepository currencies,
                      EmployeeRepository employees, UserRepository users,
                      PasswordEncoder passwordEncoder,
                      @Value("${app.seed.enabled:true}") boolean enabled,
                      @Value("${app.seed.employees:10000}") int employeeCount,
                      @Value("${app.seed.hr-email}") String hrEmail,
                      @Value("${app.seed.hr-password}") String hrPassword) {
        this.countries = countries;
        this.currencies = currencies;
        this.employees = employees;
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.enabled = enabled;
        this.employeeCount = employeeCount;
        this.hrEmail = hrEmail;
        this.hrPassword = hrPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!enabled || employees.count() > 0) {
            return;
        }
        seedCurrencies();
        List<Country> seededCountries = seedCountries();
        users.save(new User(hrEmail, passwordEncoder.encode(hrPassword)));

        Random random = new Random(42); // fixed seed: deterministic data
        String[] first = {"Aarav", "Rahul", "Priya", "Sohan", "Meera", "John", "Emma", "Liam",
                "Sofia", "Kenji", "Fatima", "Lucas", "Aisha", "Diego", "Chen", "Amara", "Ivan", "Yuki"};
        String[] last = {"Sharma", "Patel", "Khan", "Smith", "Garcia", "Muller", "Tanaka",
                "Silva", "Nguyen", "Kumar", "Ali", "Brown", "Kim", "Singh", "Chen", "Lopez"};
        // Typical annual salaries per country in local currency.
        long[][] bands = {
                {600000, 3000000}, // India INR
                {50000, 180000},   // USA USD
                {40000, 120000},   // UK GBP
                {45000, 130000},   // Germany EUR
                {3000000, 9000000},// Japan JPY
                {600000, 2000000}, // South Africa ZAR
                {80000, 250000},   // UAE AED
                {70000, 220000},   // Singapore SGD
        };

        List<Employee> batch = new ArrayList<>(1000);
        for (int i = 1; i <= employeeCount; i++) {
            Country c = seededCountries.get(random.nextInt(seededCountries.size()));
            int band = seededCountries.indexOf(c);
            long salary = bands[band][0] + (long) (random.nextDouble() * (bands[band][1] - bands[band][0]));
            String fn = first[random.nextInt(first.length)];
            String ln = last[random.nextInt(last.length)];
            batch.add(new Employee(
                    "EMP" + String.format("%05d", i),
                    fn, ln,
                    (fn + "." + ln + i + "@company.com").toLowerCase(),
                    c, c.getDefaultCurrencyCode(), BigDecimal.valueOf(salary).setScale(2, RoundingMode.HALF_UP)));
            if (batch.size() == 1000) {
                employees.saveAll(batch);
                employees.flush();
                batch.clear();
            }
        }
        if (!batch.isEmpty()) {
            employees.saveAll(batch);
        }
    }

    private void seedCurrencies() {
        List<Currency> list = List.of(
                new Currency("INR", "Indian Rupee", new BigDecimal("0.012")),
                new Currency("USD", "US Dollar", new BigDecimal("1.0")),
                new Currency("GBP", "British Pound", new BigDecimal("1.27")),
                new Currency("EUR", "Euro", new BigDecimal("1.08")),
                new Currency("JPY", "Japanese Yen", new BigDecimal("0.0067")),
                new Currency("ZAR", "South African Rand", new BigDecimal("0.055")),
                new Currency("AED", "UAE Dirham", new BigDecimal("0.272")),
                new Currency("SGD", "Singapore Dollar", new BigDecimal("0.74")));
        currencies.saveAll(list);
    }

    private List<Country> seedCountries() {
        return countries.saveAll(List.of(
                new Country("India", "IN", "INR"),
                new Country("United States", "US", "USD"),
                new Country("United Kingdom", "UK", "GBP"),
                new Country("Germany", "DE", "EUR"),
                new Country("Japan", "JP", "JPY"),
                new Country("South Africa", "ZA", "ZAR"),
                new Country("United Arab Emirates", "AE", "AED"),
                new Country("Singapore", "SG", "SGD")));
    }
}
