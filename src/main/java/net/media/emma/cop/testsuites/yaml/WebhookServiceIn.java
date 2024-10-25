package net.media.emma.cop.testsuites.yaml;

import org.springframework.stereotype.Service;

@Service
public interface WebhookServiceIn {

    void executeWebhook(String payload);
}
