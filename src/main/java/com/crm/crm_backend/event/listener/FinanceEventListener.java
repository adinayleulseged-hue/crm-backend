package com.crm.crm_backend.event.listener;

import com.crm.crm_backend.config.CrmRabbitMQConfig;
import com.crm.crm_backend.event.dto.DomainEventMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class FinanceEventListener {

    @RabbitListener(queues = CrmRabbitMQConfig.FINANCE_QUEUE)
    public void handleFinanceEvent(DomainEventMessage event) {
        log.info("Finance integration received event={} data={}", event.getEventName(), event.getData());
    }
}
