package com.acme.salary_management.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private final JwtService jwtService =
            new JwtService("test-secret-key-that-is-long-enough-for-hmac-sha256-0123456789", 60_000);

    @Test
    void generatesTokenThatRoundTripsSubject() {
        String token = jwtService.generateToken("hr@company.com");

        assertThat(jwtService.extractSubject(token)).isEqualTo("hr@company.com");
    }

    @Test
    void rejectsGarbageToken() {
        assertThatThrownBy(() -> jwtService.extractSubject("not-a-jwt"))
                .isInstanceOf(Exception.class);
    }

    @Test
    void rejectsTokenSignedWithDifferentSecret() {
        String forged = new JwtService("another-secret-key-also-long-enough-0123456789abcdef", 60_000)
                .generateToken("attacker@evil.com");

        assertThatThrownBy(() -> jwtService.extractSubject(forged))
                .isInstanceOf(Exception.class);
    }
}
