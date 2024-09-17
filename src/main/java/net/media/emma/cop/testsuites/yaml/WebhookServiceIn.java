package net.media.emma.cop.testsuites.yaml;

import org.springframework.stereotype.Service;

@Service
public interface WebhookServiceIn {

	public void executeWebhook(String payload);
}
