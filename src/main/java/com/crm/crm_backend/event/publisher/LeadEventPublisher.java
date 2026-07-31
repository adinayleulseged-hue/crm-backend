package com.crm.crm_backend.event.publisher;


import com.crm.crm_backend.model.entity.Lead;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;

import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LeadEventPublisher {
    public void publishLeadCreated(Lead lead) {

        log.info("Lead Created {}", lead.getId());

    }
    public void publishLeadQualified(Lead lead) {
        log.info("Lead qualified: {}", lead.getId());
    }

    public void publishLeadConverted(Lead lead) {
        log.info("Lead converted: {}", lead.getId());
    }
}
