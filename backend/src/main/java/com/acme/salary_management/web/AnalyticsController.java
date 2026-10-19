package com.acme.salary_management.web;

import com.acme.salary_management.service.AnalyticsService;
import com.acme.salary_management.web.dto.MedianResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/salaries")
public class AnalyticsController {

    private final AnalyticsService analytics;

    public AnalyticsController(AnalyticsService analytics) {
        this.analytics = analytics;
    }

    @GetMapping("/median")
    public MedianResponse median(@RequestParam(required = false) Long countryId) {
        return analytics.median(countryId);
    }
}
