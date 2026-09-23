package com.cafelio.api.service;

import com.cafelio.api.dto.PasswordResetConfirmRequest;
import com.cafelio.api.model.PasswordResetToken;
import com.cafelio.api.model.User;
import com.cafelio.api.repository.PasswordResetTokenRepository;
import com.cafelio.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    private PasswordResetService service;

    @BeforeEach
    void setUp() {
        service = new PasswordResetService(
                userRepository,
                tokenRepository,
                passwordEncoder,
                emailService,
                "http://127.0.0.1:5500/frontend"
        );
    }

    // requestReset

    @Test
    void solicitar_emailExistente_guardaOHashDoTokenEEnviaEmail() {
        User user = new User();
        user.setEmail("ana@example.com");

        when(userRepository.findByEmail("ana@example.com")).thenReturn(Optional.of(user));

        service.requestReset("ana@example.com");

        verify(tokenRepository).deleteByUser(user);

        ArgumentCaptor<PasswordResetToken> tokenSalvo = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokenRepository).save(tokenSalvo.capture());

        ArgumentCaptor<String> link = ArgumentCaptor.forClass(String.class);
        verify(emailService).enviarRecuperacaoDeSenha(eq("ana@example.com"), link.capture());

        String tokenDoLink = link.getValue().substring(link.getValue().indexOf("token=") + 6);

        assertThat(tokenSalvo.getValue().getTokenHash()).isEqualTo(sha256(tokenDoLink));
        assertThat(tokenSalvo.getValue().getTokenHash()).isNotEqualTo(tokenDoLink);
        assertThat(tokenSalvo.getValue().getExpiresAt()).isAfter(Instant.now());
    }

    @Test
    void solicitar_emailInexistente_naoGeraNadaNemEnvia() {
        when(userRepository.findByEmail("naoexiste@example.com")).thenReturn(Optional.empty());

        service.requestReset("naoexiste@example.com");

        verify(tokenRepository, never()).save(any());
        verify(emailService, never()).enviarRecuperacaoDeSenha(any(), any());
    }

    @Test
    void solicitar_falhaNoEnvio_naoPropagaErro() {
        User user = new User();
        user.setEmail("ana@example.com");

        when(userRepository.findByEmail("ana@example.com")).thenReturn(Optional.of(user));
        doThrow(new RuntimeException("servidor de e-mail fora do ar"))
                .when(emailService).enviarRecuperacaoDeSenha(any(), any());

        assertThatCode(() -> service.requestReset("ana@example.com"))
                .doesNotThrowAnyException();

        verify(tokenRepository).save(any());
    }

    // resetPassword

    @Test
    void redefinir_tokenValido_trocaSenhaEMarcaComoUsado() {
        User user = new User();

        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setExpiresAt(Instant.now().plusSeconds(600));

        when(tokenRepository.findByTokenHash(any())).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("NovaSenha123")).thenReturn("hash-novo");

        service.resetPassword(pedido("token-qualquer", "NovaSenha123", "NovaSenha123"));

        assertThat(user.getPasswordHash()).isEqualTo("hash-novo");
        assertThat(token.getUsedAt()).isNotNull();
        verify(userRepository).save(user);
        verify(tokenRepository).save(token);
    }

    @Test
    void redefinir_tokenInexistente_lancaExcecao() {
        when(tokenRepository.findByTokenHash(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.resetPassword(pedido("inventado", "NovaSenha123", "NovaSenha123")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Link inválido ou expirado");

        verify(userRepository, never()).save(any());
    }

    @Test
    void redefinir_tokenJaUsado_lancaExcecao() {
        PasswordResetToken token = new PasswordResetToken();
        token.setUser(new User());
        token.setExpiresAt(Instant.now().plusSeconds(600));
        token.setUsedAt(Instant.now());

        when(tokenRepository.findByTokenHash(any())).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.resetPassword(pedido("usado", "NovaSenha123", "NovaSenha123")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Link inválido ou expirado");

        verify(userRepository, never()).save(any());
    }

    @Test
    void redefinir_tokenExpirado_lancaExcecao() {
        PasswordResetToken token = new PasswordResetToken();
        token.setUser(new User());
        token.setExpiresAt(Instant.now().minusSeconds(60));

        when(tokenRepository.findByTokenHash(any())).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.resetPassword(pedido("vencido", "NovaSenha123", "NovaSenha123")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Link inválido ou expirado");

        verify(userRepository, never()).save(any());
    }

    @Test
    void redefinir_senhasDiferentes_lancaExcecaoAntesDeOlharOToken() {
        assertThatThrownBy(() -> service.resetPassword(pedido("qualquer", "NovaSenha123", "OutraSenha123")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("As senhas não coincidem");

        verify(tokenRepository, never()).findByTokenHash(any());
    }

    private PasswordResetConfirmRequest pedido(String token, String nova, String confirmacao) {
        PasswordResetConfirmRequest request = new PasswordResetConfirmRequest();
        request.setToken(token);
        request.setNewPassword(nova);
        request.setConfirmNewPassword(confirmacao);
        return request;
    }

    private String sha256(String valor) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(valor.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}