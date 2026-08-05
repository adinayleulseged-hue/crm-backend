package com.crm.crm_backend.notification.NotificationService;

import com.crm.crm_backend.event.dto.DomainEventMessage;
import com.crm.crm_backend.integration.client.NotificationClient;
import com.crm.crm_backend.integration.dto.NotificationDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationClient notificationClient;

    @Value("${crm.notification.default-email:ops@crm.local}")
    private String defaultEmail;

    @Value("${crm.notification.default-sms:+10000000000}")
    private String defaultSms;

    public void handleDomainEvent(DomainEventMessage event) {
        String subject = "[CRM] " + event.getEventName();
        String body = buildBody(event);

        NotificationDTO email = notificationClient.sendEmail(defaultEmail, subject, body);
        NotificationDTO sms = notificationClient.sendSms(defaultSms, subject + " — see email for details");

        log.info("Notification completed event={} emailDelivered={} smsDelivered={}",
                event.getEventName(), email.isDelivered(), sms.isDelivered());
    }

    private String buildBody(DomainEventMessage event) {
        StringBuilder sb = new StringBuilder();
        sb.append("Event: ").append(event.getEventName()).append('\n');
        sb.append("Source: ").append(event.getSource()).append('\n');
        sb.append("Occurred: ").append(event.getOccurredAt()).append('\n');
        sb.append("Data:\n");
        Map<String, Object> data = event.getData();
        if (data != null) {
            data.forEach((k, v) -> sb.append("  ").append(k).append('=').append(v).append('\n'));
        }
        return sb.toString();
    }
}
