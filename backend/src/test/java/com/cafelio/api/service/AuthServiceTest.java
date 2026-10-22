package com.cafelio.api.service;

import com.cafelio.api.model.User;
import com.cafelio.api.repository.UserRepository;
import com.cafelio.api.dto.LoginRequest;
import com.cafelio.api.dto.ChangePasswordRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
        when(userRepository.existsByEmail("ana@example.com")).thenReturn(false);
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
    void jaExisteContaComEsseEmail_recusaEMandaVincularNoPerfil() {
        when(userRepository.findByGoogleId("google-123")).thenReturn(Optional.empty());
        when(userRepository.existsByEmail("ana@example.com")).thenReturn(true);

        // Vincular sozinho pelo e-mail quebrava quando a pessoa trocava de
        // endereco, e deixava tomar conta alheia por coincidencia.
        assertThatThrownBy(() ->
                authService.loginOrRegisterWithGoogle("google-123", "ana@example.com", "Ana"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Já existe uma conta com esse e-mail. Entre com sua senha e vincule o Google no Perfil.");

        verify(userRepository, never()).save(any());
    }

    @Test
    void vincularGoogle_contaLivre_guardaOGoogleId() {
        UUID userId = UUID.randomUUID();
        User user = new User();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findByGoogleId("google-123")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        authService.vincularGoogle(userId, "google-123");

        assertThat(user.getGoogleId()).isEqualTo("google-123");
    }

    @Test
    void vincularGoogle_googleDeOutraConta_recusa() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);

        User outra = new User();
        outra.setId(UUID.randomUUID());

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findByGoogleId("google-123")).thenReturn(Optional.of(outra));

        assertThatThrownBy(() -> authService.vincularGoogle(userId, "google-123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Essa conta Google já está vinculada a outra conta do Cafélio");

        verify(userRepository, never()).save(any());
    }

    @Test
    void vincularGoogle_mesmoGoogleJaVinculado_naoReclama() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setGoogleId("google-123");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findByGoogleId("google-123")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        authService.vincularGoogle(userId, "google-123");

        assertThat(user.getGoogleId()).isEqualTo("google-123");
    }

    @Test
    void desvincularGoogle_contaComSenha_removeOVinculo() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setGoogleId("google-123");
        user.setPasswordHash("hash");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        authService.desvincularGoogle(userId);

        assertThat(user.getGoogleId()).isNull();
    }

    @Test
    void desvincularGoogle_contaSemSenha_recusaParaNaoTrancarAPessoaDeFora() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setGoogleId("google-123");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.desvincularGoogle(userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Defina uma senha antes de desvincular o Google — sem ela você ficaria sem como entrar.");

        assertThat(user.getGoogleId()).isEqualTo("google-123");
        verify(userRepository, never()).save(any());
    }

    @Test
    void nomeDeUsuarioJaExiste_adicionaSufixoNumerico() {
        when(userRepository.findByGoogleId("google-123")).thenReturn(Optional.empty());
        when(userRepository.existsByEmail("ana@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("ana")).thenReturn(true);
        when(userRepository.existsByUsername("ana1")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = authService.loginOrRegisterWithGoogle("google-123", "ana@example.com", "Ana");

        assertThat(result.getUsername()).isEqualTo("ana1");
    }

    @Test
    void loginComEmail_retornaUsuario() {
        User user = new User();
        user.setEmail("ana@example.com");
        user.setPasswordHash("hash-da-senha");

        when(userRepository.findByEmail("ana@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Senha123", "hash-da-senha")).thenReturn(true);

        User result = authService.login(loginRequest("ana@example.com", "Senha123"));

        assertThat(result).isSameAs(user);
    }

    @Test
    void loginComUsername_retornaUsuario() {
        User user = new User();
        user.setUsername("ana");
        user.setPasswordHash("hash-da-senha");

        when(userRepository.findByEmail("ana")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("ana")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Senha123", "hash-da-senha")).thenReturn(true);

        User result = authService.login(loginRequest("ana", "Senha123"));

        assertThat(result).isSameAs(user);
    }

    @Test
    void senhaIncorreta_lancaExcecao() {
        User user = new User();
        user.setPasswordHash("hash-da-senha");

        when(userRepository.findByEmail("ana@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("errada", "hash-da-senha")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(loginRequest("ana@example.com", "errada")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("E-mail/usuário ou senha incorretos");
    }

    @Test
    void usuarioInexistente_lancaExcecao() {
        when(userRepository.findByEmail("naoexiste@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("naoexiste@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(loginRequest("naoexiste@example.com", "Senha123")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("E-mail/usuário ou senha incorretos");
    }

    @Test
    void contaCriadaComGoogle_naoPermiteLoginComSenha() {
        User user = new User();
        user.setEmail("ana@example.com");
        user.setGoogleId("google-123");

        when(userRepository.findByEmail("ana@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(loginRequest("ana@example.com", "Senha123")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("E-mail/usuário ou senha incorretos");

        verify(passwordEncoder, never()).matches(any(), any());
    }

    private LoginRequest loginRequest(String identifier, String password) {
        LoginRequest request = new LoginRequest();
        request.setIdentifier(identifier);
        request.setPassword(password);
        return request;
    }

    @Test
    void alterarSenha_senhaAtualCorreta_salvaNovoHash() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setPasswordHash("hash-antigo");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Senha123", "hash-antigo")).thenReturn(true);
        when(passwordEncoder.encode("NovaSenha123")).thenReturn("hash-novo");

        authService.changePassword(userId, changePasswordRequest("Senha123", "NovaSenha123", "NovaSenha123"));

        assertThat(user.getPasswordHash()).isEqualTo("hash-novo");
        verify(userRepository).save(user);
    }

    @Test
    void alterarSenha_senhaAtualIncorreta_lancaExcecao() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setPasswordHash("hash-antigo");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("errada", "hash-antigo")).thenReturn(false);

        assertThatThrownBy(() -> authService.changePassword(userId,
                changePasswordRequest("errada", "NovaSenha123", "NovaSenha123")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Senha atual incorreta");

        verify(userRepository, never()).save(any());
    }

    @Test
    void alterarSenha_confirmacaoDiferente_lancaExcecao() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setPasswordHash("hash-antigo");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.changePassword(userId,
                changePasswordRequest("Senha123", "NovaSenha123", "OutraSenha123")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("As senhas não coincidem");

        verify(userRepository, never()).save(any());
    }

    @Test
    void definirSenha_contaGoogleSemSenha_naoExigeSenhaAtual() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setGoogleId("google-123");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("NovaSenha123")).thenReturn("hash-novo");

        authService.changePassword(userId, changePasswordRequest(null, "NovaSenha123", "NovaSenha123"));

        assertThat(user.getPasswordHash()).isEqualTo("hash-novo");
        verify(passwordEncoder, never()).matches(any(), any());
        verify(userRepository).save(user);
    }

    private ChangePasswordRequest changePasswordRequest(String current, String newPassword, String confirmation) {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword(current);
        request.setNewPassword(newPassword);
        request.setConfirmNewPassword(confirmation);
        return request;
    }

}
