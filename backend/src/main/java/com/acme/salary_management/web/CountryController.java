package com.acme.salary_management.web;

import com.acme.salary_management.domain.Country;
import com.acme.salary_management.repository.CountryRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/countries")
public class CountryController {

    private final CountryRepository countries;

    public CountryController(CountryRepository countries) {
        this.countries = countries;
    }

    @GetMapping
    public List<Country> all() {
        return countries.findAll();
    }
}
