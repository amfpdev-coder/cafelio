package com.cafelio.api.service;

import com.cafelio.api.model.User;
import com.cafelio.api.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AuthService authService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder);
    }

    @Test
    void usuarioNovo_criaComGoogleIdEEmailVerificado() {
        when(userRepository.findByGoogleId("google-123")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("ana@example.com")).thenReturn(Optional.empty());
        when(userRepository.existsByUsername("ana")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = authService.loginOrRegisterWithGoogle("google-123", "ana@example.com", "Ana");

        assertThat(result.getGoogleId()).isEqualTo("google-123");
        assertThat(result.getEmail()).isEqualTo("ana@example.com");
        assertThat(result.getUsername()).isEqualTo("ana");
        assertThat(result.isEmailVerified()).isTrue();
        verify(userRepository).save(any(User.class));
    }

    @Test
    void usuarioJaTemGoogleId_retornaSemSalvarDeNovo() {
        User existente = new User();
        existente.setGoogleId("google-123");
        when(userRepository.findByGoogleId("google-123")).thenReturn(Optional.of(existente));

        User result = authService.loginOrRegisterWithGoogle("google-123", "ana@example.com", "Ana");

        assertThat(result).isSameAs(existente);
        verify(userRepository, never()).save(any());
    }

    @Test
    void usuarioExistePorEmailSemGoogleId_vinculaGoogleId() {
        User existentePorEmail = new User();
        existentePorEmail.setEmail("ana@example.com");
        when(userRepository.findByGoogleId("google-123")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("ana@example.com")).thenReturn(Optional.of(existentePorEmail));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = authService.loginOrRegisterWithGoogle("google-123", "ana@example.com", "Ana");

        assertThat(result.getGoogleId()).isEqualTo("google-123");
        verify(userRepository).save(existentePorEmail);
    }

    @Test
    void nomeDeUsuarioJaExiste_adicionaSufixoNumerico() {
        when(userRepository.findByGoogleId("google-123")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("ana@example.com")).thenReturn(Optional.empty());
        when(userRepository.existsByUsername("ana")).thenReturn(true);
        when(userRepository.existsByUsername("ana1")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = authService.loginOrRegisterWithGoogle("google-123", "ana@example.com", "Ana");

        assertThat(result.getUsername()).isEqualTo("ana1");
    }
}
