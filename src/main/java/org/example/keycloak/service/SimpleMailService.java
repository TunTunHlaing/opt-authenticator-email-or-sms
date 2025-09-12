package org.example.keycloak.service;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

public class SimpleMailService {
    private final String fromEmail;
    private final Session session;

    public SimpleMailService(String smtpHost, String smtpPort, String username, String password, String fromEmail) {
        this.fromEmail = fromEmail;

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", smtpPort);

        session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    public void send(String toEmail, String otp) throws MessagingException {
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(fromEmail));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject("Your OTP Code");
        message.setText("Your OTP is: " + otp);

        Transport.send(message);
    }
}

