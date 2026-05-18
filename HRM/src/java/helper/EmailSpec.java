/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package helper;

import java.util.*;

/**
 *
 * @author ADMIN
 */
public class EmailSpec {
    private String to;
    private String from;
    private String subject;
    private String contentTemplate; 
    private String htmlBody;
    private String textFallback;
    private Map<String,String> inlineImages;
    private Map<String,Object> variables;

    public EmailSpec() {
        this.inlineImages = new HashMap<>();
        this.variables = new HashMap<>();
    }

    public EmailSpec(String to, String from, String subject, String contentTemplate, String htmlBody, String textFallback, Map<String, String> inlineImages, Map<String, Object> variables) {
        this.to = to;
        this.from = from;
        this.subject = subject;
        this.contentTemplate = contentTemplate;
        this.htmlBody = htmlBody;
        this.textFallback = textFallback;
        this.inlineImages = inlineImages;
        this.variables = variables;
    }



    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContentTemplate() {
        return contentTemplate;
    }

    public void setContentTemplate(String contentTemplate) {
        this.contentTemplate = contentTemplate;
    }

    public String getHtmlBody() {
        return htmlBody;
    }

    public void setHtmlBody(String htmlBody) {
        this.htmlBody = htmlBody;
    }

    public String getTextFallback() {
        return textFallback;
    }

    public void setTextFallback(String textFallback) {
        this.textFallback = textFallback;
    }

    public Map<String, String> getInlineImages() {
        return inlineImages;
    }

    public void setInlineImages(Map<String, String> inlineImages) {
        this.inlineImages = inlineImages;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }
    
    public static Builder builder(){
        return new Builder();
    }
    
    public static class Builder{
        private String to;
        private String from;
        private String subject;
        private String contentTemplate;
        private String htmlBody;
        private String textFallback;
        private Map<String, String> inlineImages = new HashMap<>();
        private Map<String, Object> variables = new HashMap<>();

        public Builder to(String to) { this.to = to; return this; }
        public Builder from(String from) { this.from = from; return this; }
        public Builder subject(String subject) { this.subject = subject; return this; }
        public Builder contentTemplate(String contentTemplate) {this.contentTemplate = contentTemplate; return this;}
        public Builder htmlBody(String htmlBody) { this.htmlBody = htmlBody; return this; }
        public Builder textFallback(String textFallback) { this.textFallback = textFallback; return this; }

        public Builder var(String key, Object value) {
            this.variables.put(key, value);
            return this;
        }

        public Builder inlineImage(String cid, String path) {
            this.inlineImages.put(cid, path);
            return this;
        }

        public EmailSpec build() {
            return new EmailSpec(to, from, subject,contentTemplate, htmlBody, textFallback, inlineImages, variables);
        }       
    }
    
    
}
