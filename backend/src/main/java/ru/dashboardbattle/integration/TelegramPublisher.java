package ru.dashboardbattle.integration;

import ru.dashboardbattle.dto.PublicationResultDto;
import ru.dashboardbattle.dto.TopNReportDto;

// публикация в телеграм (пока мок)
public interface TelegramPublisher {

    // отправить отчёт в канал
    PublicationResultDto publish(TopNReportDto reportDto, String botToken, String chatId);

    // удалить опубликованное сообщение
    boolean cancelPublication(String botToken, String chatId, String externalId);
}
