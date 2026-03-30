package ru.dashboardbattle.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import ru.dashboardbattle.dto.*;
import ru.dashboardbattle.entity.*;
import ru.dashboardbattle.exception.ConflictException;
import ru.dashboardbattle.exception.IntegrationException;
import ru.dashboardbattle.exception.NotFoundException;
import ru.dashboardbattle.exception.UnsupportedChannelException;
import ru.dashboardbattle.integration.MoySkladClient;
import ru.dashboardbattle.integration.TelegramPublisher;
import ru.dashboardbattle.mapper.TopNMapper;
import ru.dashboardbattle.repository.*;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DashboardBattleService {
    private static final Logger log = LoggerFactory.getLogger(DashboardBattleService.class);

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final MoySkladIntegrationRepository moyskladIntegrationRepository;
    private final TelegramIntegrationRepository telegramIntegrationRepository;
    private final TopNReportRepository topNReportRepository;
    private final TopNEntryRepository topNEntryRepository;
    private final PublishChannelRepository publishChannelRepository;
    private final PublishDestinationRepository publishDestinationRepository;
    private final PublicationRepository publicationRepository;

    private final MoySkladClient moySkladClient;
    private final TelegramPublisher telegramPublisher;
    private final String moyskladTokenOverride;
    private final String telegramTokenOverride;
    private final String telegramChatIdOverride;

    public DashboardBattleService(
            UserRepository userRepository,
            CompanyRepository companyRepository,
            MoySkladIntegrationRepository moyskladIntegrationRepository,
            TelegramIntegrationRepository telegramIntegrationRepository,
            TopNReportRepository topNReportRepository,
            TopNEntryRepository topNEntryRepository,
            PublishChannelRepository publishChannelRepository,
            PublishDestinationRepository publishDestinationRepository,
            PublicationRepository publicationRepository,
            MoySkladClient moySkladClient,
            TelegramPublisher telegramPublisher,
            @org.springframework.beans.factory.annotation.Value("${integration.moysklad.token-override:}") String moyskladTokenOverride,
            @org.springframework.beans.factory.annotation.Value("${integration.telegram.bot-token-override:}") String telegramTokenOverride,
            @org.springframework.beans.factory.annotation.Value("${integration.telegram.chat-id-override:}") String telegramChatIdOverride) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.moyskladIntegrationRepository = moyskladIntegrationRepository;
        this.telegramIntegrationRepository = telegramIntegrationRepository;
        this.topNReportRepository = topNReportRepository;
        this.topNEntryRepository = topNEntryRepository;
        this.publishChannelRepository = publishChannelRepository;
        this.publishDestinationRepository = publishDestinationRepository;
        this.publicationRepository = publicationRepository;
        this.moySkladClient = moySkladClient;
        this.telegramPublisher = telegramPublisher;
        this.moyskladTokenOverride = moyskladTokenOverride;
        this.telegramTokenOverride = telegramTokenOverride;
        this.telegramChatIdOverride = telegramChatIdOverride;
    }

    // --- регистрация ---

    public RegistrationResponseDto register(RegistrationRequestDto request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ConflictException("Пользователь с таким email уже существует: " + request.getEmail());
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(request.getPassword()); // TODO: хешировать пароль
        user = userRepository.save(user);

        Company company = new Company();
        company.setName(request.getCompanyName());
        company.setUser(user);
        company = companyRepository.save(company);

        RegistrationResponseDto response = new RegistrationResponseDto();
        response.setUserId(user.getId());
        response.setCompanyId(company.getId());
        response.setEmail(user.getEmail());
        response.setCompanyName(company.getName());
        return response;
    }

    // --- интеграции ---

    public IntegrationDataDto getIntegrationData(Long companyId) {
        IntegrationDataDto dto = new IntegrationDataDto();
        dto.setCompanyId(companyId);

        List<IntegrationDataDto.MoySkladInfoDto> msInfos = moyskladIntegrationRepository
                .findAllByCompany_Id(companyId).stream()
                .map(ms -> {
                    IntegrationDataDto.MoySkladInfoDto info = new IntegrationDataDto.MoySkladInfoDto();
                    info.setId(ms.getId());
                    info.setStatus(ms.getStatus());
                    return info;
                })
                .collect(Collectors.toList());
        dto.setMoySkladIntegrations(msInfos);

        List<IntegrationDataDto.TelegramInfoDto> tgInfos = telegramIntegrationRepository
                .findAllByCompany_Id(companyId).stream()
                .map(tg -> {
                    IntegrationDataDto.TelegramInfoDto info = new IntegrationDataDto.TelegramInfoDto();
                    info.setId(tg.getId());
                    info.setChannelChatId(tg.getChannelChatId());
                    info.setStatus(tg.getStatus());
                    return info;
                })
                .collect(Collectors.toList());
        dto.setTelegramIntegrations(tgInfos);

        return dto;
    }

    // --- запрос топ-N ---

    public TopNReportDto requestTopN(Long companyId, int topN) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new NotFoundException("Компания не найдена: " + companyId));

        MoySkladIntegration msIntegration = moyskladIntegrationRepository
                .findByCompany_Id(companyId)
                .orElseThrow(() -> new NotFoundException("Интеграция МойСклад не найдена для компании: " + companyId));

        String moyskladToken = resolveMoySkladToken(msIntegration);
        TopNReportDto fetched = moySkladClient.fetchTopN(moyskladToken, topN);

        // сразу сохраняем в БД со статусом PENDING
        TopNReport entity = TopNMapper.toEntity(fetched, company);
        entity.setStatus("PENDING");
        entity = topNReportRepository.save(entity);

        return TopNMapper.toDto(entity);
    }

    // --- одобрение топ-N ---

    public TopNReportDto confirmTopN(Long reportId) {
        TopNReport report = topNReportRepository.findById(reportId)
                .orElseThrow(() -> new NotFoundException("Отчёт не найден: " + reportId));

        report.setStatus("CONFIRMED");
        report = topNReportRepository.save(report);

        return TopNMapper.toDto(report);
    }

    // --- публикация ---

    public PublicationResultDto publishTopN(Long reportId, Long destinationId) {
        TopNReport report = topNReportRepository.findById(reportId)
                .orElseThrow(() -> new NotFoundException("Отчёт не найден: " + reportId));

        PublishDestination destination = publishDestinationRepository.findById(destinationId)
                .orElseThrow(() -> new NotFoundException("Назначение публикации не найдено: " + destinationId));

        PublishChannel channel = destination.getChannel();

        // сохраняем запись о публикации со статусом PUBLISHING
        Publication publication = new Publication();
        publication.setReport(report);
        publication.setDestination(destination);
        publication.setStatus("PUBLISHING");
        publication = publicationRepository.save(publication);

        report.setStatus("PUBLISHING");
        topNReportRepository.save(report);

        // формируем DTO и отправляем в API (пока мок)
        TopNReportDto reportDto = TopNMapper.toDto(report);

        try {
            PublicationResultDto result;

            if ("TELEGRAM".equals(channel.getCode())) {
                TelegramIntegration tgIntegration = telegramIntegrationRepository
                        .findByCompany_Id(report.getCompany().getId())
                        .orElseThrow(() -> new NotFoundException("Telegram интеграция не найдена"));

                result = telegramPublisher.publish(
                        reportDto,
                        resolveTelegramToken(tgIntegration),
                        resolveTelegramChatId(destination, tgIntegration)
                );
            } else {
                throw new UnsupportedChannelException(channel.getCode());
            }

            publication.setExternalId(result.getExternalId());
            publication.setStatus("PUBLISHED");
            publicationRepository.save(publication);

            report.setStatus("PUBLISHED");
            report.setPublishedAt(Instant.now());
            topNReportRepository.save(report);

            result.setPublicationId(publication.getId());
            result.setChannelId(channel.getId());
            result.setDestinationId(destination.getId());
            return result;

        } catch (Exception e) {
            log.error("Ошибка публикации reportId={}, destinationId={}: {}", reportId, destinationId, e.getMessage(), e);
            publication.setStatus("FAILED");
            publicationRepository.save(publication);

            report.setStatus("PUBLISH_FAILED");
            topNReportRepository.save(report);

            if (e instanceof NotFoundException
                    || e instanceof UnsupportedChannelException
                    || e instanceof IntegrationException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException("Ошибка публикации: " + e.getMessage(), e);
        }
    }

    // --- отмена публикации ---

    public PublicationResultDto cancelPublication(Long publicationId) {
        Publication publication = publicationRepository.findById(publicationId)
                .orElseThrow(() -> new NotFoundException("Публикация не найдена: " + publicationId));

        PublishDestination destination = publication.getDestination();
        PublishChannel channel = destination.getChannel();

        if ("TELEGRAM".equals(channel.getCode())) {
            TelegramIntegration tgIntegration = telegramIntegrationRepository
                    .findByCompany_Id(publication.getReport().getCompany().getId())
                    .orElseThrow(() -> new NotFoundException("Telegram интеграция не найдена"));

            boolean cancelled = telegramPublisher.cancelPublication(
                    resolveTelegramToken(tgIntegration),
                    resolveTelegramChatId(destination, tgIntegration),
                    publication.getExternalId()
            );

            if (!cancelled) {
                throw new RuntimeException("Не удалось отменить публикацию в Telegram");
            }
        }

        publication.setStatus("RECALLED");
        publicationRepository.save(publication);

        TopNReport report = publication.getReport();
        report.setStatus("RECALLED");
        topNReportRepository.save(report);

        PublicationResultDto result = new PublicationResultDto();
        result.setPublicationId(publication.getId());
        result.setChannelId(channel.getId());
        result.setDestinationId(destination.getId());
        result.setStatus("RECALLED");
        result.setExternalId(publication.getExternalId());
        return result;
    }

    // --- списки ---

    public List<TopNReportDto> listReports(Long companyId, String status) {
        List<TopNReport> reports;
        if (status != null && !status.isBlank()) {
            reports = topNReportRepository.findByCompany_IdAndStatus(companyId, status);
        } else {
            reports = topNReportRepository.findByCompany_Id(companyId);
        }
        return reports.stream()
                .map(TopNMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<PublicationResultDto> listPublications(Long companyId, Long channelId, Long destinationId) {
        List<Publication> pubs;

        if (destinationId != null) {
            pubs = publicationRepository.findByDestination_Id(destinationId);
        } else if (channelId != null) {
            pubs = publicationRepository.findByDestination_Company_IdAndDestination_Channel_Id(companyId, channelId);
        } else {
            pubs = publicationRepository.findByDestination_Company_Id(companyId);
        }

        return pubs.stream().map(pub -> {
            PublicationResultDto dto = new PublicationResultDto();
            dto.setPublicationId(pub.getId());
            dto.setChannelId(pub.getDestination().getChannel().getId());
            dto.setDestinationId(pub.getDestination().getId());
            dto.setStatus(pub.getStatus());
            dto.setExternalId(pub.getExternalId());
            return dto;
        }).collect(Collectors.toList());
    }

    // Геттеры для обратной совместимости с существующими тестами
    public UserRepository getUserRepository() { return userRepository; }
    public CompanyRepository getCompanyRepository() { return companyRepository; }
    public MoySkladIntegrationRepository getMoyskladIntegrationRepository() { return moyskladIntegrationRepository; }
    public TelegramIntegrationRepository getTelegramIntegrationRepository() { return telegramIntegrationRepository; }
    public TopNReportRepository getTopNReportRepository() { return topNReportRepository; }
    public TopNEntryRepository getTopNEntryRepository() { return topNEntryRepository; }

    private String resolveTelegramToken(TelegramIntegration integration) {
        String headerValue = getHeader("X-Debug-Telegram-Token");
        if (StringUtils.hasText(headerValue)) {
            return headerValue;
        }
        return StringUtils.hasText(telegramTokenOverride)
                ? telegramTokenOverride
                : integration.getBotTokenEncrypted();
    }

    private String resolveTelegramChatId(PublishDestination destination, TelegramIntegration integration) {
        String headerValue = getHeader("X-Debug-Telegram-Chat-Id");
        if (StringUtils.hasText(headerValue)) {
            return headerValue;
        }
        if (StringUtils.hasText(telegramChatIdOverride)) {
            return telegramChatIdOverride;
        }
        if (StringUtils.hasText(destination.getExternalIdentifier())) {
            return destination.getExternalIdentifier();
        }
        return integration.getChannelChatId();
    }

    private String resolveMoySkladToken(MoySkladIntegration integration) {
        String headerValue = getHeader("X-Debug-Moysklad-Token");
        if (StringUtils.hasText(headerValue)) {
            return headerValue;
        }
        return StringUtils.hasText(moyskladTokenOverride)
                ? moyskladTokenOverride
                : integration.getTokenEncrypted();
    }

    private String getHeader(String headerName) {
        var attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes servletAttrs) {
            return servletAttrs.getRequest().getHeader(headerName);
        }
        return null;
    }
}
