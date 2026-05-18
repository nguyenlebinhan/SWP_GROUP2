/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import helper.EmailSpec;
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
    
    private Session createSession(){
        Properties props = new Properties();
        props.put("mail.smtp.host", HOST);
        props.put("mail.smtp.port", PORT);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.ssl.trust", HOST);

        return Session.getInstance(props, new Authenticator(){
            @Override
            protected PasswordAuthentication getPasswordAuthentication(){
                return new PasswordAuthentication(FROM_EMAIL, APP_PASSWORD);
            }
        });
    }
    
    public void sendHtml(String to, String subject, String html){
        EMAIL_EXECUTOR.submit(()->{
           Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
           try{
               MimeMessage msg = new MimeMessage(createSession());
               msg.setFrom(new InternetAddress(FROM_EMAIL));
               msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
               msg.setSubject(subject,"UTF-8");
               msg.setContent(html,"text/html;charset=UTF-8");
               Transport.send(msg);
               LOGGER.log(Level.INFO,"Email sent to "+to);
           } catch (MessagingException e) {
               LOGGER.log(Level.SEVERE, "sendHtml failed to " + to, e);
           }
        });
    }
    
    public void sendBulkEmails(String subject,String content,List<String>recipients){
        recipients.forEach(email -> sendHtml(email,subject,content));
    }
    
    public void send(EmailSpec spec){
        if (FROM_EMAIL == null || FROM_EMAIL.isBlank()) {
            LOGGER.severe("send(EmailSpec) aborted: EMAIL_FROM is not configured in .env");
            return;
        }
        if (APP_PASSWORD == null || APP_PASSWORD.isBlank()) {
            LOGGER.severe("send(EmailSpec) aborted: EMAIL_APP_PASSWORD is not configured in .env");
            return;
        }
        EMAIL_EXECUTOR.submit(()->{
           Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
           try{
               String subject = spec.getSubject();
               String html = spec.getHtmlBody();
               String text = spec.getTextFallback() != null ? spec.getTextFallback() : (html != null ? stripHtml(html) : "");

               MimeMessage message = new MimeMessage(createSession());
               message.setFrom(new InternetAddress(spec.getFrom() != null ? spec.getFrom() : FROM_EMAIL));
               message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(spec.getTo()));
               message.setSubject(subject,"UTF-8");
               
               MimeMultipart multiPart = new MimeMultipart("alternative");

               MimeBodyPart textPart = new MimeBodyPart();
               textPart.setText(text, "UTF-8");
               multiPart.addBodyPart(textPart);

               if (html != null) {
                   MimeBodyPart htmlPart = new MimeBodyPart();
                   htmlPart.setContent(html, "text/html;charset=UTF-8");
                   multiPart.addBodyPart(htmlPart);
               }
               
               if(spec.getInlineImages() != null && !spec.getInlineImages().isEmpty()){
                   MimeMultipart related = new MimeMultipart("related");
                   
                   MimeBodyPart htmlWrapper = new MimeBodyPart();
                   htmlWrapper.setContent(multiPart);
                   related.addBodyPart(htmlWrapper);
                   
                   for(Map.Entry<String,String> entry : spec.getInlineImages().entrySet()){
                       String cid = entry.getKey();
                       String path = entry.getValue();
                       InputStream is = getClass().getClassLoader().getResourceAsStream(path);
                       if(is != null){
                           MimeBodyPart imagePart = new MimeBodyPart();
                           imagePart.setDataHandler(new DataHandler(new ByteArrayDataSource(is,"image/png")));
                           imagePart.setHeader("Content-ID", "<"+ cid + ">");
                           imagePart.setDisposition(MimeBodyPart.INLINE);
                           related.addBodyPart(imagePart);
                       }
                   }
                   message.setContent(related);
               }else{
                   message.setContent(multiPart);
               }
               Transport.send(message);
               LOGGER.log(Level.INFO,"Email sent to "+ spec.getTo()+ " subject: "+subject);
           } catch (MessagingException | IOException e) {
               LOGGER.log(Level.SEVERE, "send(EmailSpec) failed to " + spec.getTo(), e);
               if (e.getCause() != null) {
                   LOGGER.log(Level.SEVERE, "Root cause: " + e.getCause().getClass().getName() + ": " + e.getCause().getMessage());
               }
           }
        });
    }
    
    
    public void sendPasswordResetEmail(String to, String name, String password) {
        String html = "<div style='font-family:sans-serif;max-width:480px;margin:auto'>"
                + "<h2 style='color:#1565c0'>Đặt lại mật khẩu</h2>"
                + "<p>Xin chào <strong>" + name + "</strong>,</p>"
                + "<p>Mật khẩu mới của bạn là:</p>"
                + "<p style='font-size:1.3em;font-weight:bold;letter-spacing:2px;color:#333'>" + password + "</p>"
                + "<p style='color:#888;font-size:.85em'>Liên kết có hiệu lực trong <strong>15 phút</strong>. "
                + "Vui lòng đổi mật khẩu sau khi đăng nhập.</p>"
                + "</div>";
        EmailSpec spec = EmailSpec.builder()
                .to(to)
                .from(FROM_EMAIL)
                .subject("Đặt lại mật khẩu của bạn")
                .htmlBody(html)
                .textFallback("Xin chào " + name + ". Mật khẩu mới của bạn là: " + password)
                .build();
        send(spec);
    }
    
    private String stripHtml(String html){
        return html.replaceAll("<[^>]*>","").trim();
    }
    
    public void shutdown(){
        EMAIL_EXECUTOR.shutdown();
    }
}
