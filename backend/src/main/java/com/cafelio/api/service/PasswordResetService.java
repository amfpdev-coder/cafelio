package com.cafelio.api.service;

import com.cafelio.api.dto.PasswordResetCodeRequest;
import com.cafelio.api.dto.PasswordResetConfirmRequest;
import com.cafelio.api.model.PasswordResetToken;
import com.cafelio.api.model.User;
import com.cafelio.api.repository.PasswordResetTokenRepository;
import com.cafelio.api.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);
    private static final Duration VALIDADE = Duration.ofMinutes(30);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final String frontendUrl;

    public PasswordResetService(
            UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService,
            @Value("${cafelio.frontend-url}") String frontendUrl
    ) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.frontendUrl = frontendUrl;
    }

    @Transactional
    public void requestReset(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            tokenRepository.deleteByUser(user);

            String token = gerarToken();
            String codigo = gerarCodigo();

            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setUser(user);
            resetToken.setTokenHash(hash(token));
            resetToken.setCodeHash(hash(codigo));
            resetToken.setExpiresAt(Instant.now().plus(VALIDADE));

            tokenRepository.save(resetToken);

            String link = frontendUrl + "/index.html?token=" + token;

            try {
                emailService.enviarRecuperacaoDeSenha(email, link, codigo);
                log.info("E-mail de recuperacao enviado para {}", email);
            } catch (Exception e) {
                log.error("Falha ao enviar e-mail de recuperacao para {}", email, e);
            }
        });
    }

    @Transactional
    public void resetPassword(PasswordResetConfirmRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new IllegalArgumentException("As senhas não coincidem");
        }

        PasswordResetToken resetToken = tokenRepository.findByTokenHash(hash(request.getToken()))
                .orElseThrow(() -> new IllegalArgumentException("Link inválido ou expirado"));

        if (resetToken.isUsado() || resetToken.isExpirado()) {
            throw new IllegalArgumentException("Link inválido ou expirado");
        }

        trocarSenha(resetToken, request.getNewPassword());
    }

    @Transactional(noRollbackFor = IllegalArgumentException.class)
    public void resetPasswordByCode(PasswordResetCodeRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new IllegalArgumentException("As senhas não coincidem");
        }

        PasswordResetToken resetToken = userRepository.findByEmail(request.getEmail())
                .flatMap(tokenRepository::findByUser)
                .orElseThrow(() -> new IllegalArgumentException("Código inválido ou expirado"));

        if (resetToken.isUsado() || resetToken.isExpirado() || resetToken.isBloqueadoPorTentativas()) {
            throw new IllegalArgumentException("Código inválido ou expirado");
        }

        if (resetToken.getCodeHash() == null
                || !resetToken.getCodeHash().equals(hash(request.getCode()))) {
            resetToken.registrarTentativaErrada();
            tokenRepository.save(resetToken);
            throw new IllegalArgumentException("Código inválido ou expirado");
        }

        trocarSenha(resetToken, request.getNewPassword());
    }

    private void trocarSenha(PasswordResetToken resetToken, String novaSenha) {
        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(novaSenha));
        userRepository.save(user);

        resetToken.setUsedAt(Instant.now());
        tokenRepository.save(resetToken);
    }

    private String gerarToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String gerarCodigo() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }

    private String hash(String valor) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] resumo = digest.digest(valor.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(resumo);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algoritmo SHA-256 indisponivel", e);
        }
    }
}