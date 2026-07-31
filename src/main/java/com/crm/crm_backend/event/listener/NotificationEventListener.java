package com.crm.crm_backend.event.listener;

import com.crm.crm_backend.config.CrmRabbitMQConfig;
import com.crm.crm_backend.event.dto.DomainEventMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NotificationEventListener {

    @RabbitListener(queues = CrmRabbitMQConfig.NOTIFICATION_QUEUE)
    public void handleDomainEvent(DomainEventMessage event) {
        log.info("Notification service received event={} data={}", event.getEventName(), event.getData());
    }
}
