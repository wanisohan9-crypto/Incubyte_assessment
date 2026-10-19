package com.acme.salary_management.service;

import com.acme.salary_management.domain.User;
import com.acme.salary_management.repository.UserRepository;
import com.acme.salary_management.security.JwtService;
import com.acme.salary_management.web.dto.LoginRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    private final UserRepository users = mock(UserRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final JwtService jwtService = mock(JwtService.class);
    private final AuthService authService = new AuthService(users, passwordEncoder, jwtService);

    @Test
    void returnsTokenForValidCredentials() {
        User user = new User("hr@company.com", "hashed");
        when(users.findByEmail("hr@company.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password123!", "hashed")).thenReturn(true);
        when(jwtService.generateToken("hr@company.com")).thenReturn("jwt-token");

        String token = authService.login(new LoginRequest("hr@company.com", "Password123!"));

        assertThat(token).isEqualTo("jwt-token");
    }

    @Test
    void rejectsWrongPassword() {
        when(users.findByEmail("hr@company.com")).thenReturn(Optional.of(new User("hr@company.com", "hashed")));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("hr@company.com", "nope")))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.UNAUTHORIZED);
    }

    @Test
    void rejectsUnknownEmail() {
        when(users.findByEmail("ghost@company.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("ghost@company.com", "x")))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.UNAUTHORIZED);
    }
}
