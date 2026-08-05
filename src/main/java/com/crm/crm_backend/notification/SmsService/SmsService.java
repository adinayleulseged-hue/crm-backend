package com.crm.crm_backend.notification.SmsService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SmsService {

    @Value("${crm.notification.sms.enabled:true}")
    private boolean enabled;

    @Value("${crm.notification.sms.provider:log}")
    private String provider;

    public boolean send(String phoneNumber, String message) {
        if (!enabled) {
            log.info("SMS disabled — would send to={} message={}", phoneNumber, message);
            return false;
        }

        // Real dispatch path: provider hook (Twilio/etc.) can replace log transport later.
        log.info("SMS SENT via provider={} to={} message={}", provider, phoneNumber, message);
        return true;
    }
}
