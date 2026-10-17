package com.cafelio.api.controller;

import com.cafelio.api.dto.AuthResponse;
import com.cafelio.api.dto.GoogleLoginRequest;
import com.cafelio.api.dto.RegisterRequest;
import com.cafelio.api.dto.UserResponse;
import com.cafelio.api.dto.LoginRequest;
import com.cafelio.api.dto.PasswordResetConfirmRequest;
import com.cafelio.api.dto.PasswordResetRequest;
import com.cafelio.api.dto.ChangePasswordRequest;
import com.cafelio.api.dto.PasswordResetCodeRequest;
import com.cafelio.api.model.User;
import com.cafelio.api.security.GoogleTokenVerifier;
import com.cafelio.api.service.AuthService;
import com.cafelio.api.service.JwtService;
import com.cafelio.api.service.PasswordResetService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import com.cafelio.api.service.ProfileService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;


@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final GoogleTokenVerifier googleTokenVerifier;
    private final JwtService jwtService;
    private final PasswordResetService passwordResetService;
    private final ProfileService profileService;

    public AuthController(
            AuthService authService,
            GoogleTokenVerifier googleTokenVerifier,
            JwtService jwtService,
            PasswordResetService passwordResetService,
            ProfileService profileService
    ) {
        this.authService = authService;
        this.googleTokenVerifier = googleTokenVerifier;
        this.jwtService = jwtService;
        this.passwordResetService = passwordResetService;
        this.profileService = profileService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        var user = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new UserResponse(user));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        User user = authService.login(request);
        String token = jwtService.generateToken(user.getId());

        return ResponseEntity.ok(new AuthResponse(token));
    }

    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal UUID userId) {
        User user = authService.findById(userId);
        return ResponseEntity.ok(new UserResponse(user, profileService.urlDaFoto(user)));
    }

    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(
        @AuthenticationPrincipal UUID userId,
        @Valid @RequestBody ChangePasswordRequest request
    ) {
        authService.changePassword(userId, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/password/reset-request")
    public ResponseEntity<Void> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        passwordResetService.requestReset(request.getEmail());
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/password/reset")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody PasswordResetConfirmRequest request) {
        passwordResetService.resetPassword(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/password/reset-code")
    public ResponseEntity<Void> resetPasswordByCode(@Valid @RequestBody PasswordResetCodeRequest request) {
        passwordResetService.resetPasswordByCode(request);
        return ResponseEntity.noContent().build();
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

    @SecurityRequirement(name = "bearerAuth")
    @PostMapping(value = "/profile-picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserResponse> uploadProfilePicture(
            @AuthenticationPrincipal UUID userId,
            @RequestParam("file") MultipartFile file
    ) {
        User user = profileService.atualizarFoto(userId, file);
        return ResponseEntity.ok(new UserResponse(user, profileService.urlDaFoto(user)));
    }

    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/profile-picture")
    public ResponseEntity<UserResponse> removeProfilePicture(@AuthenticationPrincipal UUID userId) {
        User user = profileService.removerFoto(userId);
        return ResponseEntity.ok(new UserResponse(user, null));
    }
}