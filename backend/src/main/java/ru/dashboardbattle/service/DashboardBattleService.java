package ru.dashboardbattle.service; // Наш пакет

import org.springframework.stereotype.Service; // Указываем аннотацию для спринга
import ru.dashboardbattle.repository.*; // Импортируем все репозитории

/**
 * Главный сервис приложения (Неделя 4). Пока только заготовка: инжект всех репозиториев.
 * Дальше сюда будет добавляться логика: регистрация, ТОП-N, публикация в Telegram.
 */
@Service
public class DashboardBattleService {
    // Обьявляем бойцов:
    private final UserRepository userRepository; // Репозиторий для работы с пользователями
    private final CompanyRepository companyRepository;
    private final MoySkladIntegrationRepository moyskladIntegrationRepository;
    private final TelegramIntegrationRepository telegramIntegrationRepository;
    private final TopNReportRepository topNReportRepository;
    private final TopNEntryRepository topNEntryRepository;

    public DashboardBattleService(
            UserRepository userRepository,
            CompanyRepository companyRepository,
            MoySkladIntegrationRepository moyskladIntegrationRepository,
            TelegramIntegrationRepository telegramIntegrationRepository,
            TopNReportRepository topNReportRepository,
            TopNEntryRepository topNEntryRepository) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.moyskladIntegrationRepository = moyskladIntegrationRepository;
        this.telegramIntegrationRepository = telegramIntegrationRepository;
        this.topNReportRepository = topNReportRepository;
        this.topNEntryRepository = topNEntryRepository;
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }

    public CompanyRepository getCompanyRepository() {
        return companyRepository;
    }

    public MoySkladIntegrationRepository getMoyskladIntegrationRepository() {
        return moyskladIntegrationRepository;
    }

    public TelegramIntegrationRepository getTelegramIntegrationRepository() {
        return telegramIntegrationRepository;
    }

    public TopNReportRepository getTopNReportRepository() {
        return topNReportRepository;
    }

    public TopNEntryRepository getTopNEntryRepository() {
        return topNEntryRepository;
    }
}
