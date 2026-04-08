package ru.dashboardbattle.integration;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ru.dashboardbattle.dto.PublicationResultDto;
import ru.dashboardbattle.dto.TopNReportDto;
import ru.dashboardbattle.exception.IntegrationException;

import java.net.URI;

@Service
public class WebhookPublisher implements WebhookPublishing {

    private final RestTemplate restTemplate;

    public WebhookPublisher(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public PublicationResultDto publish(TopNReportDto reportDto, String callbackUrl) {
        URI uri = validateUrl(callbackUrl);
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(uri, reportDto, String.class);
            PublicationResultDto dto = new PublicationResultDto();
            dto.setStatus("PUBLISHED");
            dto.setExternalId("webhook-" + response.getStatusCode().value());
            return dto;
        } catch (RestClientException ex) {
            throw new IntegrationException("Ошибка вызова webhook: " + ex.getMessage());
        }
    }

    private static URI validateUrl(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IntegrationException("Для канала Webhook укажите URL в поле «Доп. идентификатор / URL» назначения публикации.");
        }
        String trimmed = raw.trim();
        URI uri;
        try {
            uri = URI.create(trimmed);
        } catch (IllegalArgumentException e) {
            throw new IntegrationException("Некорректный URL webhook.");
        }
        String scheme = uri.getScheme();
        if (scheme == null || (!"https".equalsIgnoreCase(scheme) && !"http".equalsIgnoreCase(scheme))) {
            throw new IntegrationException("URL webhook должен начинаться с http:// или https://");
        }
        return uri;
    }
}
