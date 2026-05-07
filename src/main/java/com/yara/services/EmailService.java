package com.yara.services;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // 🔥 ENVIAR OTP
    public void enviarOTP(
            String destino,
            String codigo
    ) {

        SimpleMailMessage mensaje =
                new SimpleMailMessage();

        mensaje.setTo(destino);

        mensaje.setSubject(
                "Código de verificación Yara"
        );

        mensaje.setText(
                "Tu código de verificación es: "
                        + codigo
        );

        mailSender.send(mensaje);
    }
}