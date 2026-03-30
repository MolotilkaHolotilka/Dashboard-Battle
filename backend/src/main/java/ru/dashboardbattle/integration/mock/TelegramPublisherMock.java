package ru.dashboardbattle.integration.mock;

import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import ru.dashboardbattle.dto.PublicationResultDto;
import ru.dashboardbattle.dto.TopNReportDto;
import ru.dashboardbattle.integration.TelegramPublisher;

import java.util.UUID;

// мок телеграм — имитирует отправку
@Component
@ConditionalOnProperty(name = "integration.telegram.real-enabled", havingValue = "false", matchIfMissing = true)
public class TelegramPublisherMock implements TelegramPublisher {

    @Override
    public PublicationResultDto publish(TopNReportDto reportDto, String botToken, String chatId) {
        PublicationResultDto result = new PublicationResultDto();
        result.setStatus("PUBLISHED");
        // имитируем ID сообщения в Telegram
        result.setExternalId("tg-msg-" + UUID.randomUUID().toString().substring(0, 8));
        return result;
    }

    @Override
    public boolean cancelPublication(String botToken, String chatId, String externalId) {
        // мок всегда возвращает успех
        return true;
    }
}
