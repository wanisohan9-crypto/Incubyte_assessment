package com.acme.salary_management.web;

import com.acme.salary_management.service.AuthService;
import com.acme.salary_management.web.dto.LoginRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(Map.of("token", authService.login(request)));
    }
}
