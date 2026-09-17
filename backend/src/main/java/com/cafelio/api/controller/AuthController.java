package com.cafelio.api.controller;

import com.cafelio.api.dto.AuthResponse;
import com.cafelio.api.dto.GoogleLoginRequest;
import com.cafelio.api.dto.RegisterRequest;
import com.cafelio.api.dto.UserResponse;
import com.cafelio.api.model.User;
import com.cafelio.api.security.GoogleTokenVerifier;
import com.cafelio.api.service.AuthService;
import com.cafelio.api.service.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final GoogleTokenVerifier googleTokenVerifier;
    private final JwtService jwtService;

    public AuthController(
            AuthService authService,
            GoogleTokenVerifier googleTokenVerifier,
            JwtService jwtService
    ) {
        this.authService = authService;
        this.googleTokenVerifier = googleTokenVerifier;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        var user = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new UserResponse(user));
    }

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> loginWithGoogle(@Valid @RequestBody GoogleLoginRequest request) {
        var payload = googleTokenVerifier.verify(request.getIdToken());

        String googleId = payload.getSubject();
        String email = payload.getEmail();
        String name = (String) payload.get("name");

        User user = authService.loginOrRegisterWithGoogle(googleId, email, name);
        String token = jwtService.generateToken(user.getId());

        return ResponseEntity.ok(new AuthResponse(token));
    }
}