package com.crm.crm_backend.event.listener;

import com.crm.crm_backend.config.CrmRabbitMQConfig;
import com.crm.crm_backend.event.dto.DomainEventMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class InventoryEventListener {

    @RabbitListener(queues = CrmRabbitMQConfig.INVENTORY_QUEUE)
    public void handleInventoryEvent(DomainEventMessage event) {
        log.info("Inventory/MMS integration received event={} data={}", event.getEventName(), event.getData());
    }
}
