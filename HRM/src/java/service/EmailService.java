/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import jakarta.activation.DataHandler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import jakarta.mail.Authenticator;
import jakarta.mail.Session;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.*;
import utils.ConfigManager;
/**
 *
 * @author ADMIN
 */
public class EmailService {
    private static final Logger LOGGER = Logger.getLogger(EmailService.class.getName());
    private final ConfigManager config = ConfigManager.getInstance();

    private final String HOST = config.getProperty("EMAIL_HOST", "smtp.gmail.com");
    private final String PORT = config.getProperty("EMAIL_PORT", "587");
    private final String FROM_EMAIL = config.getProperty("EMAIL_FROM");
    private final String APP_PASSWORD = config.getProperty("EMAIL_APP_PASSWORD");
    private static final ExecutorService EMAIL_EXECUTOR = Executors.newFixedThreadPool(10);
    

    public boolean sendEmail(String to, String subject, String body) {
        if (FROM_EMAIL == null || FROM_EMAIL.isBlank() || APP_PASSWORD == null || APP_PASSWORD.isBlank()) {
            LOGGER.severe("sendEmail aborted: EMAIL_FROM or EMAIL_APP_PASSWORD not configured in .env");
            return false;
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.ssl.trust", HOST);
        props.put("mail.smtp.host", HOST);
        props.put("mail.smtp.port", PORT);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, APP_PASSWORD);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject, "UTF-8");
            message.setDataHandler(new DataHandler(
                new ByteArrayDataSource(body.getBytes(StandardCharsets.UTF_8), "text/html; charset=UTF-8")));
            message.setHeader("Content-Type", "text/html; charset=UTF-8");
            Transport.send(message);
            return true;
        } catch (MessagingException e) {
            LOGGER.log(Level.SEVERE, "sendEmail failed to " + to, e);
            return false;
        }
    }    



    public void sendResetPasswordEmailAsync(String toEmail, String password) {
        EMAIL_EXECUTOR.submit(() -> {
            String subject = "Password Reset Request - Thesis Management System";
            
            String body = "<html><head><meta charset='UTF-8'></head><body>" +
                          "<p>You have requested to reset your password.</p>" +
                          "<p>Your password has been updated into the DB:</p>" +
                          "<p>Here is your password: '" + password + "'</p>" +
                          "<p>If you did not request this, please ignore this email.</p>" +
                          "</body></html>";
            
            sendEmail(toEmail, subject, body);
        });
    }
   
    
    public void shutdown(){
        EMAIL_EXECUTOR.shutdown();
    }
}
