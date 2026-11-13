package com.crm.crm_backend.util.generator;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class LeadNumberGenerator {

    public String generate() {
        return "LEAD-" +
                UUID.randomUUID()
                        .toString()
                        .substring(0, 8)
                        .toUpperCase();
    }
}