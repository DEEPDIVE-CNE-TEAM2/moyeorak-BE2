package com.example.moyeorak.controller;

import com.example.moyeorak.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class TestTokenController {

    private final JwtProvider jwtProvider;

    @GetMapping("/api/test-token")
    public ResponseEntity<?> testToken(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        boolean isValid = jwtProvider.validateToken(token);
        String email = jwtProvider.getEmail(token);
        String role = jwtProvider.getRole(token);
        return ResponseEntity.ok("✅ 유효: " + isValid + ", 이메일: " + email + ", 역할: " + role);
    }
}
