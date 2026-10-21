package com.cafelio.api.service;

import com.cafelio.api.dto.RegisterRequest;
import com.cafelio.api.dto.ChangePasswordRequest;
import com.cafelio.api.model.User;
import com.cafelio.api.repository.UserRepository;
import com.cafelio.api.dto.LoginRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

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
        user.setName(request.getName());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        return userRepository.save(user);
    }

    public User findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
    }

    public void changePassword(UUID userId, ChangePasswordRequest request) {
        User user = findById(userId);

        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new IllegalArgumentException("As senhas não coincidem");
        }

        if (user.getPasswordHash() != null) {
            if (request.getCurrentPassword() == null
                    || !passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
                throw new IllegalArgumentException("Senha atual incorreta");
            }
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    public User loginOrRegisterWithGoogle(String googleId, String email, String name) {
        var porGoogle = userRepository.findByGoogleId(googleId);

        if (porGoogle.isPresent()) {
            return porGoogle.get();
        }

        // Antes o vínculo era adivinhado pelo e-mail. Isso quebrava assim que
        // a pessoa trocava de endereço, e deixava qualquer um se apossar de
        // conta alheia por coincidência. Agora o vínculo é explícito, no Perfil.
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException(
                    "Já existe uma conta com esse e-mail. Entre com sua senha e vincule o Google no Perfil."
            );
        }

        User newUser = new User();
        String usernameGerado = generateUniqueUsername(email);

        newUser.setName(name != null && !name.isBlank() ? name : usernameGerado);
        newUser.setUsername(usernameGerado);
        newUser.setEmail(email);
        newUser.setGoogleId(googleId);
        newUser.setEmailVerified(true);

        return userRepository.save(newUser);
    }

    @Transactional
    public User vincularGoogle(UUID userId, String googleId) {
        User user = findById(userId);

        userRepository.findByGoogleId(googleId).ifPresent(outra -> {
            if (!outra.getId().equals(userId)) {
                throw new IllegalArgumentException(
                        "Essa conta Google já está vinculada a outra conta do Cafélio"
                );
            }
        });

        user.setGoogleId(googleId);
        return userRepository.save(user);
    }

    @Transactional
    public User desvincularGoogle(UUID userId) {
        User user = findById(userId);

        // Sem senha e sem Google, a pessoa ficaria trancada para fora da conta
        if (user.getPasswordHash() == null) {
            throw new IllegalArgumentException(
                    "Defina uma senha antes de desvincular o Google — sem ela você ficaria sem como entrar."
            );
        }

        user.setGoogleId(null);
        return userRepository.save(user);
    }

    public User login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getIdentifier())
                .or(() -> userRepository.findByUsername(request.getIdentifier()))
                .orElseThrow(() -> new IllegalArgumentException("E-mail/usuário ou senha incorretos"));

        if (user.getPasswordHash() == null) {
            throw new IllegalArgumentException("E-mail/usuário ou senha incorretos");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("E-mail/usuário ou senha incorretos");
        }

        return user;
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
