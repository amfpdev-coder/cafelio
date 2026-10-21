package com.cafelio.api.service;

import com.cafelio.api.model.EmailChangeRequest;
import com.cafelio.api.model.User;
import com.cafelio.api.repository.EmailChangeRequestRepository;
import com.cafelio.api.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class EmailChangeService {

    private static final Logger log = LoggerFactory.getLogger(EmailChangeService.class);
    private static final Duration VALIDADE = Duration.ofMinutes(30);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final EmailChangeRequestRepository requestRepository;
    private final EmailService emailService;

    public EmailChangeService(
            UserRepository userRepository,
            EmailChangeRequestRepository requestRepository,
            EmailService emailService
    ) {
        this.userRepository = userRepository;
        this.requestRepository = requestRepository;
        this.emailService = emailService;
    }

    @Transactional
    public void pedirTroca(UUID userId, String novoEmail) {
        User user = buscar(userId);
        String email = novoEmail.trim().toLowerCase();

        if (email.equalsIgnoreCase(user.getEmail())) {
            throw new IllegalArgumentException("Esse já é o seu e-mail");
        }

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Esse e-mail já está em uso");
        }

        // O delete precisa chegar ao banco antes do insert: user_id é único
        // e o Hibernate, por padrão, manda os inserts primeiro.
        requestRepository.deleteByUser(user);
        requestRepository.flush();

        String codigo = gerarCodigo();

        EmailChangeRequest pedido = new EmailChangeRequest();
        pedido.setUser(user);
        pedido.setNewEmail(email);
        pedido.setCodeHash(hash(codigo));
        pedido.setExpiresAt(Instant.now().plus(VALIDADE));

        requestRepository.save(pedido);

        // O código vai para o endereço ATUAL: é ele que prova que quem pede
        // é a dona da conta.
        try {
            emailService.enviarConfirmacaoDeEmail(user.getEmail(), email, codigo);
        } catch (Exception e) {
            log.error("Falha ao enviar codigo de troca de e-mail", e);
        }
    }

    @Transactional(noRollbackFor = IllegalArgumentException.class)
    public User confirmar(UUID userId, String codigo) {
        User user = buscar(userId);

        EmailChangeRequest pedido = requestRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Nenhuma troca de e-mail pendente"));

        if (pedido.getExpiresAt().isBefore(Instant.now())) {
            requestRepository.delete(pedido);
            throw new IllegalArgumentException("O código expirou. Peça um novo.");
        }

        if (pedido.getAttempts() >= EmailChangeRequest.MAX_TENTATIVAS) {
            requestRepository.delete(pedido);
            throw new IllegalArgumentException("Muitas tentativas. Peça um novo código.");
        }

        if (!pedido.getCodeHash().equals(hash(codigo))) {
            pedido.setAttempts(pedido.getAttempts() + 1);
            requestRepository.save(pedido);
            throw new IllegalArgumentException("Código inválido");
        }

        // Entre o pedido e a confirmação passam até 30 minutos, e nesse
        // intervalo alguém pode ter se cadastrado com esse endereço.
        if (userRepository.existsByEmail(pedido.getNewEmail())) {
            requestRepository.delete(pedido);
            throw new IllegalArgumentException("Esse e-mail já está em uso");
        }

        String antigo = user.getEmail();

        user.setEmail(pedido.getNewEmail());
        userRepository.save(user);
        requestRepository.delete(pedido);

        try {
            emailService.avisarTrocaDeEmail(antigo, user.getEmail());
        } catch (Exception e) {
            log.error("Falha ao avisar o e-mail antigo sobre a troca", e);
        }

        return user;
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

    private User buscar(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
    }
}
