package com.crm.crm_backend.event.publisher;

import com.crm.crm_backend.model.entity.Opportunity;
import com.crm.crm_backend.model.enums.OpportunityStage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class OpportunityEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public OpportunityEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Publish event when opportunity stage changes
     * This method is called by OpportunityServiceTest
     */
    public void publishOpportunityStageChanged(Opportunity opportunity, OpportunityStage newStage) {
        log.info("Publishing opportunity stage changed event for opportunity ID: {}, new stage: {}",
                opportunity.getId(), newStage);

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("opportunityId", opportunity.getId());
        eventData.put("previousStage", opportunity.getStage());
        eventData.put("newStage", newStage);
        eventData.put("opportunityName", opportunity.getOpportunityName());
        if (opportunity.getCustomer() != null) {
            eventData.put("customerId", opportunity.getCustomer().getId());
        }
        eventData.put("timestamp", Instant.now());

        // Send to RabbitMQ exchange
        try {
            rabbitTemplate.convertAndSend(
                    "erp.events",
                    "opportunity.stage.changed",
                    eventData
            );
        } catch (Exception e) {
            log.error("Failed to publish opportunity stage changed event: {}", e.getMessage());
        }
    }

    /**
     * Publish event when opportunity is created
     */
    public void publishOpportunityCreated(Opportunity opportunity) {
        log.info("Publishing opportunity created event for opportunity ID: {}", opportunity.getId());

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("opportunityId", opportunity.getId());
        eventData.put("opportunityName", opportunity.getOpportunityName());
        if (opportunity.getCustomer() != null) {
            eventData.put("customerId", opportunity.getCustomer().getId());
        }
        eventData.put("stage", opportunity.getStage());
        eventData.put("amount", opportunity.getExpectedRevenue());
        eventData.put("timestamp", Instant.now());

        try {
            rabbitTemplate.convertAndSend(
                    "erp.events",
                    "opportunity.created",
                    eventData
            );
        } catch (Exception e) {
            log.error("Failed to publish opportunity created event: {}", e.getMessage());
        }
    }
}