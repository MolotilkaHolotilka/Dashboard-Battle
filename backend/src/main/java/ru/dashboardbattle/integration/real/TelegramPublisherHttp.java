package ru.dashboardbattle.integration.real;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger log = LoggerFactory.getLogger(TelegramPublisherHttp.class);

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
        log.info("Sending Telegram publication: chatId={}, entries={}", maskChatId(chatId), entryCount(reportDto));

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            Map<String, Object> body = response.getBody();
            if (body == null || !Boolean.TRUE.equals(body.get("ok"))) {
                log.warn("Telegram sendMessage returned unsuccessful response: status={}, body={}",
                        response.getStatusCode(), body);
                throw new IntegrationException("Telegram вернул некорректный ответ при публикации");
            }

            Object resultObj = body.get("result");
            if (!(resultObj instanceof Map<?, ?> result)) {
                throw new IntegrationException("Telegram не вернул result при публикации");
            }

            Object messageId = result.get("message_id");
            if (messageId == null) {
                log.warn("Telegram sendMessage response does not contain message_id: {}", body);
                throw new IntegrationException("Telegram не вернул message_id");
            }

            PublicationResultDto dto = new PublicationResultDto();
            dto.setStatus("PUBLISHED");
            dto.setExternalId(String.valueOf(messageId));
            log.info("Telegram publication created successfully: chatId={}, messageId={}",
                    maskChatId(chatId), dto.getExternalId());
            return dto;
        } catch (RestClientException ex) {
            log.error("Telegram sendMessage failed for chatId={}: {}", maskChatId(chatId), ex.getMessage(), ex);
            throw new IntegrationException("Ошибка вызова Telegram API: " + ex.getMessage(), ex);
        }
    }

    @Override
    public boolean cancelPublication(String botToken, String chatId, String externalId) {
        String url = "https://api.telegram.org/bot" + botToken + "/deleteMessage";
        log.info("Cancelling Telegram publication: chatId={}, externalId={}", maskChatId(chatId), externalId);

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
            boolean ok = body != null && Boolean.TRUE.equals(body.get("ok"));
            log.info("Telegram deleteMessage finished: chatId={}, externalId={}, ok={}",
                    maskChatId(chatId), externalId, ok);
            return ok;
        } catch (NumberFormatException ex) {
            log.error("Telegram deleteMessage received invalid externalId={}", externalId, ex);
            throw new IntegrationException("Некорректный externalId для Telegram: " + externalId, ex);
        } catch (RestClientException ex) {
            log.error("Telegram deleteMessage failed for chatId={}, externalId={}: {}",
                    maskChatId(chatId), externalId, ex.getMessage(), ex);
            throw new IntegrationException("Ошибка отмены публикации в Telegram: " + ex.getMessage(), ex);
        }
    }

    private static int entryCount(TopNReportDto reportDto) {
        return reportDto == null || reportDto.getEntries() == null ? 0 : reportDto.getEntries().size();
    }

    private static String maskChatId(String raw) {
        if (raw == null || raw.isBlank()) {
            return "<empty>";
        }
        if (raw.length() <= 4) {
            return "****";
        }
        return "****" + raw.substring(raw.length() - 4);
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
