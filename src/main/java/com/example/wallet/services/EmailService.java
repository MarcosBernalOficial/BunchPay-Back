package com.example.wallet.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendPdfByEmail(String toEmail, byte[] pdfBytes) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom("no-reply@tuapp.com"); // Remitente fijo
        helper.setTo(toEmail);
        helper.setSubject("Tu comprobante PDF");
        helper.setText("Adjuntamos tu comprobante PDF.");

        // Ponemos el PDF adjunto
        helper.addAttachment("comprobante.pdf", new ByteArrayResource(pdfBytes));

        mailSender.send(message);
    }
}
