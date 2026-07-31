package com.crm.crm_backend.util.generator;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class CustomerNumberGenerator {

    private static final String PREFIX = "CUST-";
    private static final AtomicLong counter = new AtomicLong(1);

    public String generate() {
        String year = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy"));
        long sequence = counter.getAndIncrement();
        return String.format("%s%s-%06d", PREFIX, year, sequence);
    }
}
