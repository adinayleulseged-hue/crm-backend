package com.crm.crm_backend.event.publisher;

import com.crm.crm_backend.config.CrmRabbitMQConfig;
import com.crm.crm_backend.event.CrmRoutingKeys;
import com.crm.crm_backend.event.dto.DomainEventMessage;
import com.crm.crm_backend.model.entity.SalesOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishSalesOrderConfirmed(SalesOrder order) {
        Map<String, Object> data = new HashMap<>();
        data.put("orderId", order.getId());
        data.put("orderNumber", order.getOrderNumber());
        data.put("customerId", order.getCustomer() != null ? order.getCustomer().getId() : null);
        data.put("totalAmount", order.getTotalAmount());
        data.put("status", order.getStatus() != null ? order.getStatus().name() : null);

        publish(CrmRoutingKeys.SALES_ORDER_CONFIRMED, data);
    }

    private void publish(String routingKey, Map<String, Object> data) {
        DomainEventMessage message = DomainEventMessage.builder()
                .eventName(routingKey)
                .source("crm-backend")
                .occurredAt(LocalDateTime.now())
                .data(data)
                .build();

        rabbitTemplate.convertAndSend(CrmRabbitMQConfig.ERP_EVENTS_EXCHANGE, routingKey, message);
        log.info("Published event {} for order {}", routingKey, data.get("orderNumber"));
    }
}
