package ru.dashboardbattle.integration.real;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ru.dashboardbattle.dto.PublicationResultDto;
import ru.dashboardbattle.dto.TopNEntryDto;
import ru.dashboardbattle.dto.TopNReportDto;
import ru.dashboardbattle.exception.IntegrationException;
import ru.dashboardbattle.integration.TelegramPublisher;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(name = "integration.telegram.real-enabled", havingValue = "true")
public class TelegramPublisherHttp implements TelegramPublisher {

    private final RestTemplate restTemplate;

    @Autowired
    public TelegramPublisherHttp(RestTemplateBuilder restTemplateBuilder) {
        this(restTemplateBuilder.build());
    }

    public TelegramPublisherHttp(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public PublicationResultDto publish(TopNReportDto reportDto, String botToken, String chatId) {
        String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("chat_id", chatId);
        request.put("text", buildMessage(reportDto));

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            Map<String, Object> body = response.getBody();
            if (body == null || !Boolean.TRUE.equals(body.get("ok"))) {
                throw new IntegrationException("Telegram вернул некорректный ответ при публикации");
            }

            Object resultObj = body.get("result");
            if (!(resultObj instanceof Map<?, ?> result)) {
                throw new IntegrationException("Telegram не вернул result при публикации");
            }

            Object messageId = result.get("message_id");
            if (messageId == null) {
                throw new IntegrationException("Telegram не вернул message_id");
            }

            PublicationResultDto dto = new PublicationResultDto();
            dto.setStatus("PUBLISHED");
            dto.setExternalId(String.valueOf(messageId));
            return dto;
        } catch (RestClientException ex) {
            throw new IntegrationException("Ошибка вызова Telegram API: " + ex.getMessage(), ex);
        }
    }

    @Override
    public boolean cancelPublication(String botToken, String chatId, String externalId) {
        String url = "https://api.telegram.org/bot" + botToken + "/deleteMessage";

        try {
            Map<String, Object> request = Map.of(
                    "chat_id", chatId,
                    "message_id", Long.parseLong(externalId)
            );
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<>(request),
                    Map.class
            );
            Map<String, Object> body = response.getBody();
            return body != null && Boolean.TRUE.equals(body.get("ok"));
        } catch (NumberFormatException ex) {
            throw new IntegrationException("Некорректный externalId для Telegram: " + externalId, ex);
        } catch (RestClientException ex) {
            throw new IntegrationException("Ошибка отмены публикации в Telegram: " + ex.getMessage(), ex);
        }
    }

    private String buildMessage(TopNReportDto reportDto) {
        List<TopNEntryDto> entries = reportDto.getEntries();
        String header = "TOP-N отчёт\nСтатус: " + reportDto.getStatus();
        if (entries == null || entries.isEmpty()) {
            return header + "\nНет данных.";
        }

        String lines = entries.stream()
                .map(e -> String.format(
                        "%d. %s | выручка: %s | маржа: %s | товар: %s",
                        e.getRank(),
                        safe(e.getEmployeeName()),
                        e.getRevenue(),
                        e.getMargin(),
                        safe(e.getFavoriteProduct())
                ))
                .collect(Collectors.joining("\n"));
        return header + "\n\n" + lines;
    }

    private String safe(String value) {
        return value == null ? "-" : value;
    }
}
