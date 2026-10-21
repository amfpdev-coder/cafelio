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

    public void enviarConfirmacaoDeEmail(String destinatario, String novoEmail, String codigo) {
        SimpleMailMessage mensagem = new SimpleMailMessage();

        mensagem.setFrom(remetente);
        mensagem.setTo(destinatario);
        mensagem.setSubject("Cafélio — Código para trocar seu e-mail");
        mensagem.setText("""
                Olá!

                Recebemos um pedido para mudar o e-mail da sua conta no Cafélio
                para:

                %s

                Para confirmar, informe este código na tela de perfil:

                %s

                O código vale por 30 minutos.

                Se não foi você que pediu, NÃO informe o código e troque sua
                senha — alguém pode ter acesso à sua conta.

                Até logo,
                Cafélio
                """.formatted(novoEmail, codigo));

        mailSender.send(mensagem);
    }

    public void avisarTrocaDeEmail(String destinatario, String novoEmail) {
        SimpleMailMessage mensagem = new SimpleMailMessage();

        mensagem.setFrom(remetente);
        mensagem.setTo(destinatario);
        mensagem.setSubject("Cafélio — Seu e-mail foi alterado");
        mensagem.setText("""
                Olá!

                O e-mail da sua conta no Cafélio acabou de ser alterado para:

                %s

                Se foi você, não precisa fazer nada.

                Se NÃO foi você, alguém pode ter acesso à sua conta. Entre em
                contato com a gente o quanto antes.

                Até logo,
                Cafélio
                """.formatted(novoEmail));

        mailSender.send(mensagem);
    }
}