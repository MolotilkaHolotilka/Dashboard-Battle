package ru.dashboardbattle.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.dashboardbattle.dto.*;
import ru.dashboardbattle.entity.*;
import ru.dashboardbattle.integration.DemoPagePublishing;
import ru.dashboardbattle.integration.MoySkladClient;
import ru.dashboardbattle.integration.TelegramPublisher;
import ru.dashboardbattle.integration.WebhookPublishing;
import ru.dashboardbattle.repository.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardBattleServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private CompanyRepository companyRepository;
    @Mock private MoySkladIntegrationRepository moyskladIntegrationRepository;
    @Mock private TelegramIntegrationRepository telegramIntegrationRepository;
    @Mock private TopNReportRepository topNReportRepository;
    @Mock private TopNEntryRepository topNEntryRepository;
    @Mock private PublishChannelRepository publishChannelRepository;
    @Mock private PublishDestinationRepository publishDestinationRepository;
    @Mock private PublicationRepository publicationRepository;
    @Mock private MoySkladClient moySkladClient;
    @Mock private TelegramPublisher telegramPublisher;
    @Mock private DemoPagePublishing demoPagePublisher;
    @Mock private WebhookPublishing webhookPublisher;
    @Mock private PasswordEncoder passwordEncoder;

    private DashboardBattleService service;

    private Company testCompany;
    private User testUser;

    @BeforeEach
    void setUp() {
        service = new DashboardBattleService(
                userRepository,
                companyRepository,
                moyskladIntegrationRepository,
                telegramIntegrationRepository,
                topNReportRepository,
                topNEntryRepository,
                publishChannelRepository,
                publishDestinationRepository,
                publicationRepository,
                moySkladClient,
                telegramPublisher,
                demoPagePublisher,
                webhookPublisher,
                passwordEncoder,
                "",
                "",
                "",
                false
        );

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@test.ru");
        testUser.setPasswordHash("hash");

        testCompany = new Company();
        testCompany.setId(10L);
        testCompany.setName("Test Company");
        testCompany.setUser(testUser);
    }

    // ========== Регистрация ==========

    @Test
    void register_shouldSaveUserAndCompany() {
        when(passwordEncoder.encode(anyString())).thenAnswer(inv -> "ENC_" + inv.getArgument(0));
        RegistrationRequestDto request = new RegistrationRequestDto("user@mail.ru", "pass123", "Компания");

        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(companyRepository.save(any(Company.class))).thenAnswer(inv -> {
            Company c = inv.getArgument(0);
            c.setId(10L);
            return c;
        });

        RegistrationResponseDto result = service.register(request);

        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getCompanyId()).isEqualTo(10L);
        assertThat(result.getEmail()).isEqualTo("user@mail.ru");
        assertThat(result.getCompanyName()).isEqualTo("Компания");

        verify(userRepository).save(any(User.class));
        verify(companyRepository).save(any(Company.class));
        verify(passwordEncoder).encode("pass123");
    }

    // ========== Получение данных интеграций ==========

    @Test
    void getIntegrationData_shouldReturnMoySkladAndTelegram() {
        MoySkladIntegration ms = new MoySkladIntegration();
        ms.setId(100L);
        ms.setStatus("ACTIVE");

        TelegramIntegration tg = new TelegramIntegration();
        tg.setId(200L);
        tg.setChannelChatId("-10012345");
        tg.setStatus("ACTIVE");

        when(moyskladIntegrationRepository.findAllByCompany_Id(10L)).thenReturn(List.of(ms));
        when(telegramIntegrationRepository.findAllByCompany_Id(10L)).thenReturn(List.of(tg));

        IntegrationDataDto data = service.getIntegrationData(10L);

        assertThat(data.getCompanyId()).isEqualTo(10L);
        assertThat(data.getMoySkladIntegrations()).hasSize(1);
        assertThat(data.getTelegramIntegrations()).hasSize(1);
        assertThat(data.getTelegramIntegrations().get(0).getChannelChatId()).isEqualTo("••••2345");

        verify(moyskladIntegrationRepository).findAllByCompany_Id(10L);
        verify(telegramIntegrationRepository).findAllByCompany_Id(10L);
    }

    // ========== Запрос ТОП-N ==========

    @Test
    void requestTopN_shouldCallMoySkladAndSaveReport() {
        MoySkladIntegration msIntegration = new MoySkladIntegration();
        msIntegration.setTokenEncrypted("token123");

        TopNReportDto mockFetched = new TopNReportDto();
        mockFetched.setPeriodStart(LocalDate.of(2026, 2, 1));
        mockFetched.setPeriodEnd(LocalDate.of(2026, 3, 1));
        mockFetched.setStatus("PENDING");
        mockFetched.setEntries(List.of(
                new TopNEntryDto("Иванов", "ms-001", new BigDecimal("1500000"), new BigDecimal("350000"), "Ноутбук", 1)
        ));

        when(companyRepository.findById(10L)).thenReturn(Optional.of(testCompany));
        when(moyskladIntegrationRepository.findByCompany_Id(10L)).thenReturn(Optional.of(msIntegration));
        when(moySkladClient.fetchTopN("token123", 5)).thenReturn(mockFetched);
        when(topNReportRepository.save(any(TopNReport.class))).thenAnswer(inv -> {
            TopNReport r = inv.getArgument(0);
            r.setId(50L);
            r.setCreatedAt(Instant.now());
            return r;
        });

        TopNReportDto result = service.requestTopN(10L, 5);

        assertThat(result.getId()).isEqualTo(50L);
        assertThat(result.getStatus()).isEqualTo("PENDING");
        assertThat(result.getEntries()).hasSize(1);

        verify(moySkladClient).fetchTopN("token123", 5);
        verify(topNReportRepository).save(any(TopNReport.class));
    }

    @Test
    void requestTopN_shouldNormalizeBearerPrefixInStoredToken() {
        MoySkladIntegration msIntegration = new MoySkladIntegration();
        msIntegration.setTokenEncrypted("Bearer token123");

        TopNReportDto mockFetched = new TopNReportDto();
        mockFetched.setPeriodStart(LocalDate.of(2026, 2, 1));
        mockFetched.setPeriodEnd(LocalDate.of(2026, 3, 1));
        mockFetched.setStatus("PENDING");
        mockFetched.setEntries(List.of(
                new TopNEntryDto("Иванов", "ms-001", new BigDecimal("1500000"), new BigDecimal("350000"), "Ноутбук", 1)
        ));

        when(companyRepository.findById(10L)).thenReturn(Optional.of(testCompany));
        when(moyskladIntegrationRepository.findByCompany_Id(10L)).thenReturn(Optional.of(msIntegration));
        when(moySkladClient.fetchTopN("token123", 5)).thenReturn(mockFetched);
        when(topNReportRepository.save(any(TopNReport.class))).thenAnswer(inv -> {
            TopNReport r = inv.getArgument(0);
            r.setId(51L);
            r.setCreatedAt(Instant.now());
            return r;
        });

        TopNReportDto result = service.requestTopN(10L, 5);

        assertThat(result.getId()).isEqualTo(51L);
        verify(moySkladClient).fetchTopN("token123", 5);
    }

    @Test
    void requestTopN_shouldPreferIntegrationTokenOverEnvOverride() {
        DashboardBattleService serviceWithOverride = new DashboardBattleService(
                userRepository,
                companyRepository,
                moyskladIntegrationRepository,
                telegramIntegrationRepository,
                topNReportRepository,
                topNEntryRepository,
                publishChannelRepository,
                publishDestinationRepository,
                publicationRepository,
                moySkladClient,
                telegramPublisher,
                demoPagePublisher,
                webhookPublisher,
                passwordEncoder,
                "override-token",
                "",
                "",
                false
        );

        MoySkladIntegration msIntegration = new MoySkladIntegration();
        msIntegration.setTokenEncrypted("db-token");

        TopNReportDto mockFetched = new TopNReportDto();
        mockFetched.setPeriodStart(LocalDate.of(2026, 2, 1));
        mockFetched.setPeriodEnd(LocalDate.of(2026, 3, 1));
        mockFetched.setStatus("PENDING");
        mockFetched.setEntries(List.of(
                new TopNEntryDto("Иванов", "ms-001", new BigDecimal("1500000"), new BigDecimal("350000"), "Ноутбук", 1)
        ));

        when(companyRepository.findById(10L)).thenReturn(Optional.of(testCompany));
        when(moyskladIntegrationRepository.findByCompany_Id(10L)).thenReturn(Optional.of(msIntegration));
        when(moySkladClient.fetchTopN("db-token", 5)).thenReturn(mockFetched);
        when(topNReportRepository.save(any(TopNReport.class))).thenAnswer(inv -> {
            TopNReport r = inv.getArgument(0);
            r.setId(52L);
            r.setCreatedAt(Instant.now());
            return r;
        });

        TopNReportDto result = serviceWithOverride.requestTopN(10L, 5);

        assertThat(result.getId()).isEqualTo(52L);
        verify(moySkladClient).fetchTopN("db-token", 5);
    }

    @Test
    void requestTopN_shouldThrowIfCompanyNotFound() {
        when(companyRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.requestTopN(999L, 5))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Компания не найдена");
    }

    // ========== Одобрение ТОП-N ==========

    @Test
    void confirmTopN_shouldChangeStatusToConfirmed() {
        TopNReport report = new TopNReport();
        report.setId(50L);
        report.setCompany(testCompany);
        report.setStatus("PENDING");
        report.setEntries(new ArrayList<>());

        when(topNReportRepository.findById(50L)).thenReturn(Optional.of(report));
        when(topNReportRepository.save(any(TopNReport.class))).thenAnswer(inv -> inv.getArgument(0));

        TopNReportDto result = service.confirmTopN(50L);

        assertThat(result.getStatus()).isEqualTo("CONFIRMED");
        verify(topNReportRepository).save(argThat(r -> "CONFIRMED".equals(r.getStatus())));
    }

    // ========== Публикация ==========

    @Test
    void publishTopN_shouldCallTelegramAndSavePublication() {
        TopNReport report = new TopNReport();
        report.setId(50L);
        report.setCompany(testCompany);
        report.setStatus("CONFIRMED");
        report.setEntries(new ArrayList<>());

        PublishChannel channel = new PublishChannel();
        channel.setId(1L);
        channel.setCode("TELEGRAM");
        channel.setName("Telegram");

        PublishDestination destination = new PublishDestination();
        destination.setId(5L);
        destination.setChannel(channel);
        destination.setCompany(testCompany);
        destination.setLabel("Основной канал");
        destination.setExternalIdentifier("-10012345");

        TelegramIntegration tgIntegration = new TelegramIntegration();
        tgIntegration.setBotTokenEncrypted("bot-token");

        PublicationResultDto mockResult = new PublicationResultDto();
        mockResult.setStatus("PUBLISHED");
        mockResult.setExternalId("tg-msg-abc12345");

        when(topNReportRepository.findById(50L)).thenReturn(Optional.of(report));
        when(publishDestinationRepository.findById(5L)).thenReturn(Optional.of(destination));
        when(telegramIntegrationRepository.findByCompany_Id(10L)).thenReturn(Optional.of(tgIntegration));
        when(telegramPublisher.publish(any(), eq("bot-token"), eq("-10012345"))).thenReturn(mockResult);
        when(publicationRepository.save(any(Publication.class))).thenAnswer(inv -> {
            Publication p = inv.getArgument(0);
            p.setId(77L);
            return p;
        });
        when(topNReportRepository.save(any(TopNReport.class))).thenAnswer(inv -> inv.getArgument(0));

        PublicationResultDto result = service.publishTopN(50L, 5L);

        assertThat(result.getStatus()).isEqualTo("PUBLISHED");
        assertThat(result.getPublicationId()).isEqualTo(77L);
        assertThat(result.getExternalId()).isEqualTo("tg-msg-abc12345");

        verify(telegramPublisher).publish(any(), eq("bot-token"), eq("-10012345"));
        verify(publicationRepository, times(2)).save(any(Publication.class));
    }

    // ========== Отмена публикации ==========

    @Test
    void cancelPublication_shouldCallTelegramAndMarkRecalled() {
        PublishChannel channel = new PublishChannel();
        channel.setId(1L);
        channel.setCode("TELEGRAM");
        channel.setName("Telegram");

        PublishDestination destination = new PublishDestination();
        destination.setId(5L);
        destination.setChannel(channel);
        destination.setCompany(testCompany);
        destination.setExternalIdentifier("-10012345");

        TopNReport report = new TopNReport();
        report.setId(50L);
        report.setCompany(testCompany);

        Publication publication = new Publication();
        publication.setId(77L);
        publication.setReport(report);
        publication.setDestination(destination);
        publication.setExternalId("tg-msg-abc12345");
        publication.setStatus("PUBLISHED");

        TelegramIntegration tgIntegration = new TelegramIntegration();
        tgIntegration.setBotTokenEncrypted("bot-token");

        when(publicationRepository.findById(77L)).thenReturn(Optional.of(publication));
        when(telegramIntegrationRepository.findByCompany_Id(10L)).thenReturn(Optional.of(tgIntegration));
        when(telegramPublisher.cancelPublication("bot-token", "-10012345", "tg-msg-abc12345")).thenReturn(true);
        when(publicationRepository.save(any(Publication.class))).thenAnswer(inv -> inv.getArgument(0));
        when(topNReportRepository.save(any(TopNReport.class))).thenAnswer(inv -> inv.getArgument(0));

        PublicationResultDto result = service.cancelPublication(77L);

        assertThat(result.getStatus()).isEqualTo("RECALLED");
        verify(telegramPublisher).cancelPublication("bot-token", "-10012345", "tg-msg-abc12345");
        verify(publicationRepository).save(argThat(p -> "RECALLED".equals(p.getStatus())));
    }

    // ========== Списки ===========

    @Test
    void listReports_shouldReturnFilteredByStatus() {
        TopNReport r1 = new TopNReport();
        r1.setId(1L);
        r1.setCompany(testCompany);
        r1.setStatus("CONFIRMED");
        r1.setEntries(new ArrayList<>());

        when(topNReportRepository.findByCompany_IdAndStatusOrderByCreatedAtDesc(10L, "CONFIRMED")).thenReturn(List.of(r1));

        List<TopNReportDto> result = service.listReports(10L, "CONFIRMED");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo("CONFIRMED");
        verify(topNReportRepository).findByCompany_IdAndStatusOrderByCreatedAtDesc(10L, "CONFIRMED");
    }

    @Test
    void listReports_shouldReturnAllWhenNoStatus() {
        TopNReport r1 = new TopNReport();
        r1.setId(1L);
        r1.setCompany(testCompany);
        r1.setStatus("PENDING");
        r1.setEntries(new ArrayList<>());

        TopNReport r2 = new TopNReport();
        r2.setId(2L);
        r2.setCompany(testCompany);
        r2.setStatus("PUBLISHED");
        r2.setEntries(new ArrayList<>());

        when(topNReportRepository.findByCompany_IdOrderByCreatedAtDesc(10L)).thenReturn(List.of(r1, r2));

        List<TopNReportDto> result = service.listReports(10L, null);

        assertThat(result).hasSize(2);
        verify(topNReportRepository).findByCompany_IdOrderByCreatedAtDesc(10L);
    }

    @Test
    void listPublications_shouldFilterByDestination() {
        PublishChannel channel = new PublishChannel();
        channel.setId(1L);
        channel.setCode("TELEGRAM");
        channel.setName("Telegram");

        PublishDestination dest = new PublishDestination();
        dest.setId(5L);
        dest.setChannel(channel);
        dest.setCompany(testCompany);

        TopNReport report = new TopNReport();
        report.setId(50L);
        report.setCompany(testCompany);
        report.setPeriodStart(LocalDate.of(2026, 2, 1));
        report.setPeriodEnd(LocalDate.of(2026, 2, 28));

        Publication pub = new Publication();
        pub.setId(77L);
        pub.setDestination(dest);
        pub.setReport(report);
        pub.setStatus("PUBLISHED");
        pub.setExternalId("tg-msg-123");

        when(publicationRepository.findByDestination_Id(5L)).thenReturn(List.of(pub));

        List<PublicationResultDto> result = service.listPublications(10L, null, 5L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo("PUBLISHED");
        verify(publicationRepository).findByDestination_Id(5L);
    }
}
