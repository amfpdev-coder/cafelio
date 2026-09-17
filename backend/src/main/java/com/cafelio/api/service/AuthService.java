package com.cafelio.api.service;

import com.cafelio.api.dto.RegisterRequest;
import com.cafelio.api.model.User;
import com.cafelio.api.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("As senhas não coincidem");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Este e-mail já está cadastrado");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Este nome de usuário já está em uso");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        return userRepository.save(user);
    }

public User loginOrRegisterWithGoogle(String googleId, String email, String name) {
    return userRepository.findByGoogleId(googleId)
            .orElseGet(() -> {
                var existingByEmail = userRepository.findByEmail(email);
                if (existingByEmail.isPresent()) {
                    User user = existingByEmail.get();
                    user.setGoogleId(googleId);
                    return userRepository.save(user);
                }

                User newUser = new User();
                newUser.setUsername(generateUniqueUsername(email));
                newUser.setEmail(email);
                newUser.setGoogleId(googleId);
                newUser.setEmailVerified(true);
                return userRepository.save(newUser);
            });
}

private String generateUniqueUsername(String email) {
    String base = email.split("@")[0];
    String candidate = base;
    int suffix = 1;

    while (userRepository.existsByUsername(candidate)) {
        candidate = base + suffix;
        suffix++;
    }

    return candidate;
}
}
