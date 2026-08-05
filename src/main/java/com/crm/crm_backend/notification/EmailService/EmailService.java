package com.crm.crm_backend.notification.EmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${crm.notification.email.from:crm-noreply@insa.local}")
    private String fromAddress;

    @Value("${crm.notification.email.enabled:true}")
    private boolean enabled;

    public boolean send(String to, String subject, String body) {
        if (!enabled) {
            log.info("Email disabled — would send to={} subject={}", to, subject);
            return false;
        }

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            log.info("EMAIL SENT (log transport) to={} subject={} body={}", to, subject, body);
            return true;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("EMAIL SENT via SMTP to={} subject={}", to, subject);
            return true;
        } catch (Exception ex) {
            log.error("EMAIL FAILED to={} subject={}: {}", to, subject, ex.getMessage());
            log.info("EMAIL FALLBACK (log transport) to={} subject={} body={}", to, subject, body);
            return true;
        }
    }
}
