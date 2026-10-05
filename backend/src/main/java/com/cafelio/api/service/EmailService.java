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

    public void enviarRecuperacaoDeSenha(String destinatario, String link, String codigo) {
        SimpleMailMessage mensagem = new SimpleMailMessage();

        mensagem.setFrom(remetente);
        mensagem.setTo(destinatario);
        mensagem.setSubject("Cafélio — Recuperação de senha");
        mensagem.setText("""
                Olá!

                Recebemos um pedido para redefinir a sua senha na conta do Cafélio.
                Você pode escolher uma das duas formas abaixo.

                1) Clicar neste link:

                %s

                2) Ou informar este código na tela de recuperação:

                %s

                Qualquer uma das duas vale por 30 minutos e só pode ser usada uma vez.

                Se não foi você que pediu, pode ignorar esta mensagem — sua senha continua a mesma.

                Até logo,
                Cafélio
                """.formatted(link, codigo));

        mailSender.send(mensagem);
    }
}