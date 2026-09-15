package com.foodexpress.notification.service;


import com.foodexpress.common.message.EmailMessage;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.from:noreply@foodexpress.com}")
    private String fromEmail;

    @Value("${app.name:FoodExpress}")
    private String appName;

    public void sendEmail(EmailMessage message) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(message.getTo());
            helper.setSubject(message.getSubject());

            // Process template
            String content = processTemplate(message.getTemplateName(), message.getContext());
            helper.setText(content, true);

            if (message.getCc() != null && !message.getCc().isEmpty()) {
                helper.setCc(message.getCc().toArray(new String[0]));
            }

            mailSender.send(mimeMessage);
            logger.info("Email sent successfully to: {}", message.getTo());

        } catch (MessagingException e) {
            logger.error("Failed to send email to: {}", message.getTo(), e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    private String processTemplate(String templateName, Map<String, Object> context) {
        Context thymeleafContext = new Context();
        thymeleafContext.setVariables(context);
        thymeleafContext.setVariable("appName", appName);

        return templateEngine.process("email/" + templateName, thymeleafContext);
    }
}
