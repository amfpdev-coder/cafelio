package com.cafelio.api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final String remetente;

    public EmailService(
            JavaMailSender mailSender,
            @Value("${spring.mail.username}") String remetente
    ) {
        this.mailSender = mailSender;
        this.remetente = remetente;
    }

    public void enviarRecuperacaoDeSenha(String destinatario, String link) {
        SimpleMailMessage mensagem = new SimpleMailMessage();

        mensagem.setFrom(remetente);
        mensagem.setTo(destinatario);
        mensagem.setSubject("Cafélio — Recuperação de senha");
        mensagem.setText("""
                Olá!

                Recebemos um pedido para redefinir a sua senha no Cafélio.

                Para escolher uma senha nova, acesse o link abaixo:

                %s

                O link vale por 30 minutos e só pode ser usado uma vez.

                Se não foi você que pediu, pode ignorar esta mensagem — sua senha continua a mesma.

                Até logo,
                Cafélio
                """.formatted(link));

        mailSender.send(mensagem);
    }
}