package com.acme.salary_management.service;

import com.acme.salary_management.repository.UserRepository;
import com.acme.salary_management.security.JwtService;
import com.acme.salary_management.web.dto.LoginRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository users, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public String login(LoginRequest request) {
        var user = users.findByEmail(request.email())
                .filter(u -> u.isActive()
                        && passwordEncoder.matches(request.password(), u.getPasswordHash()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        return jwtService.generateToken(user.getEmail());
    }
}
