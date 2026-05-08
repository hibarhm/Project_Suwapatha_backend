package com.suwapatha.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Thin wrapper around the Twilio SDK for sending SMS messages.
 * Mirrors the fail-graceful pattern used in EmailService.
 */
@Service
@Slf4j
public class SmsService {

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.from-number}")
    private String fromNumber;

    @PostConstruct
    public void init() {
        Twilio.init(accountSid, authToken);
        log.info("Twilio SMS service initialised (from={})", fromNumber);
    }

    /**
     * Sends an SMS to the given phone number.
     * Fails gracefully — logs the error and returns without throwing.
     *
     * @param toNumber E.164 format, e.g. "+94771234567"
     * @param body     The message text (max ~1,600 chars)
     */
    public void sendSms(String toNumber, String body) {
        if (toNumber == null || toNumber.isBlank()) {
            log.warn("SMS skipped — no phone number provided");
            return;
        }
        try {
            Message message = Message.creator(
                    new PhoneNumber(toNumber),
                    new PhoneNumber(fromNumber),
                    body).create();
            log.info("SMS sent to {} (SID={})", toNumber, message.getSid());
        } catch (Exception e) {
            log.error("Failed to send SMS to {}: {}", toNumber, e.getMessage());
            // Fail gracefully — do not propagate; appointment flow must not break
        }
    }
}
