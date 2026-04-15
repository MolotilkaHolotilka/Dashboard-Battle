package ru.dashboardbattle.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
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
import ru.dashboardbattle.integration.DemoPagePublishing;
import ru.dashboardbattle.integration.MoySkladClient;
import ru.dashboardbattle.integration.TelegramPublisher;
import ru.dashboardbattle.integration.WebhookPublishing;
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
    private final DemoPagePublishing demoPagePublisher;
    private final WebhookPublishing webhookPublisher;

    private final PasswordEncoder passwordEncoder;

    private final String moyskladTokenOverride;
    private final String telegramTokenOverride;
    private final String telegramChatIdOverride;
    private final boolean allowDebugHeaders;

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
            DemoPagePublishing demoPagePublisher,
            WebhookPublishing webhookPublisher,
            PasswordEncoder passwordEncoder,
            @Value("${integration.moysklad.token-override:}") String moyskladTokenOverride,
            @Value("${integration.telegram.bot-token-override:}") String telegramTokenOverride,
            @Value("${integration.telegram.chat-id-override:}") String telegramChatIdOverride,
            @Value("${integration.allow-debug-headers:false}") boolean allowDebugHeaders) {
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
        this.demoPagePublisher = demoPagePublisher;
        this.webhookPublisher = webhookPublisher;
        this.passwordEncoder = passwordEncoder;
        this.moyskladTokenOverride = moyskladTokenOverride;
        this.telegramTokenOverride = telegramTokenOverride;
        this.telegramChatIdOverride = telegramChatIdOverride;
        this.allowDebugHeaders = allowDebugHeaders;
    }

    public RegistrationResponseDto register(RegistrationRequestDto request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ConflictException("Пользователь с таким email уже существует: " + request.getEmail());
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
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

    public CompanySummaryDto getCompanySummary(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new NotFoundException("Компания не найдена: " + companyId));
        CompanySummaryDto dto = new CompanySummaryDto();
        dto.setId(company.getId());
        dto.setName(company.getName());
        return dto;
    }

    public List<CompanySummaryDto> listCompaniesByUser(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + userId));
        return companyRepository.findByUser_Id(userId).stream()
                .map(company -> {
                    CompanySummaryDto dto = new CompanySummaryDto();
                    dto.setId(company.getId());
                    dto.setName(company.getName());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<PublishChannelDto> listPublishChannels() {
        return publishChannelRepository.findAll().stream()
                .map(ch -> {
                    PublishChannelDto d = new PublishChannelDto();
                    d.setId(ch.getId());
                    d.setCode(ch.getCode());
                    d.setName(ch.getName());
                    return d;
                })
                .collect(Collectors.toList());
    }

    public List<PublishDestinationDto> listPublishDestinations(Long companyId) {
        companyRepository.findById(companyId)
                .orElseThrow(() -> new NotFoundException("Компания не найдена: " + companyId));
        return publishDestinationRepository.findByCompany_Id(companyId).stream()
                .map(dest -> {
                    PublishDestinationDto d = new PublishDestinationDto();
                    d.setId(dest.getId());
                    d.setLabel(dest.getLabel());
                    d.setChannelCode(dest.getChannel().getCode());
                    d.setChannelName(dest.getChannel().getName());
                    d.setExternalIdentifier(dest.getExternalIdentifier());
                    return d;
                })
                .collect(Collectors.toList());
    }

    public PublishDestinationDto createPublishDestination(CreatePublishDestinationRequestDto body) {
        if (body.getCompanyId() == null) {
            throw new ConflictException("Укажите companyId.");
        }
        if (!StringUtils.hasText(body.getChannelCode())) {
            throw new ConflictException("Укажите channelCode (TELEGRAM, DEMO_PAGE, WEBHOOK).");
        }
        Company company = companyRepository.findById(body.getCompanyId())
                .orElseThrow(() -> new NotFoundException("Компания не найдена: " + body.getCompanyId()));
        String codeUpper = body.getChannelCode().trim().toUpperCase();
        PublishChannel channel = publishChannelRepository.findByCode(codeUpper)
                .orElseThrow(() -> new NotFoundException("Неизвестный канал: " + body.getChannelCode()));

        PublishDestination dest = new PublishDestination();
        dest.setCompany(company);
        dest.setChannel(channel);
        dest.setLabel(StringUtils.hasText(body.getLabel()) ? body.getLabel().trim() : channel.getName());
        dest.setExternalIdentifier(StringUtils.hasText(body.getExternalIdentifier()) ? body.getExternalIdentifier().trim() : null);
        dest = publishDestinationRepository.save(dest);

        PublishDestinationDto dto = new PublishDestinationDto();
        dto.setId(dest.getId());
        dto.setLabel(dest.getLabel());
        dto.setChannelCode(channel.getCode());
        dto.setChannelName(channel.getName());
        dto.setExternalIdentifier(dest.getExternalIdentifier());
        return dto;
    }

    public IntegrationDataDto upsertMoyskladIntegration(MoyskladIntegrationUpsertRequestDto body) {
        if (body.getCompanyId() == null) {
            throw new ConflictException("Укажите companyId.");
        }
        if (!StringUtils.hasText(body.getAccessToken())) {
            throw new ConflictException("Укажите токен доступа МойСклад.");
        }
        Company company = companyRepository.findById(body.getCompanyId())
                .orElseThrow(() -> new NotFoundException("Компания не найдена: " + body.getCompanyId()));

        MoySkladIntegration integration = moyskladIntegrationRepository.findByCompany_Id(body.getCompanyId())
                .orElseGet(() -> {
                    MoySkladIntegration created = new MoySkladIntegration();
                    created.setCompany(company);
                    return created;
                });
        integration.setTokenEncrypted(body.getAccessToken().trim());
        integration.setStatus("ACTIVE");
        moyskladIntegrationRepository.save(integration);
        return getIntegrationData(body.getCompanyId());
    }

    public IntegrationDataDto upsertTelegramIntegration(TelegramIntegrationUpsertRequestDto body) {
        if (body.getCompanyId() == null) {
            throw new ConflictException("Укажите companyId.");
        }
        if (!StringUtils.hasText(body.getBotToken()) || !StringUtils.hasText(body.getChannelChatId())) {
            throw new ConflictException("Укажите botToken и channelChatId Telegram.");
        }
        Company company = companyRepository.findById(body.getCompanyId())
                .orElseThrow(() -> new NotFoundException("Компания не найдена: " + body.getCompanyId()));

        TelegramIntegration integration = telegramIntegrationRepository.findByCompany_Id(body.getCompanyId())
                .orElseGet(() -> {
                    TelegramIntegration created = new TelegramIntegration();
                    created.setCompany(company);
                    return created;
                });
        integration.setBotTokenEncrypted(body.getBotToken().trim());
        integration.setChannelChatId(body.getChannelChatId().trim());
        integration.setStatus("ACTIVE");
        telegramIntegrationRepository.save(integration);
        return getIntegrationData(body.getCompanyId());
    }

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
                    info.setChannelChatId(maskChatId(tg.getChannelChatId()));
                    info.setStatus(tg.getStatus());
                    return info;
                })
                .collect(Collectors.toList());
        dto.setTelegramIntegrations(tgInfos);

        return dto;
    }

    public TopNReportDto requestTopN(Long companyId, int topN) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new NotFoundException("Компания не найдена: " + companyId));

        MoySkladIntegration msIntegration = moyskladIntegrationRepository
                .findByCompany_Id(companyId)
                .orElseThrow(() -> new NotFoundException("Интеграция МойСклад не найдена для компании: " + companyId));

        String moyskladToken = resolveMoySkladToken(msIntegration);
        TopNReportDto fetched = moySkladClient.fetchTopN(moyskladToken, topN);

        TopNReport entity = TopNMapper.toEntity(fetched, company);
        entity.setStatus("PENDING");
        entity = topNReportRepository.save(entity);

        return TopNMapper.toDto(entity);
    }

    public TopNReportDto confirmTopN(Long reportId) {
        TopNReport report = topNReportRepository.findById(reportId)
                .orElseThrow(() -> new NotFoundException("Отчёт не найден: " + reportId));

        report.setStatus("CONFIRMED");
        report = topNReportRepository.save(report);

        return TopNMapper.toDto(report);
    }

    public TopNReportDto getReportById(Long reportId) {
        TopNReport report = topNReportRepository.findById(reportId)
                .orElseThrow(() -> new NotFoundException("Отчёт не найден: " + reportId));
        return TopNMapper.toDto(report);
    }

    public PublicationResultDto publishTopN(Long reportId, Long destinationId) {
        TopNReport report = topNReportRepository.findById(reportId)
                .orElseThrow(() -> new NotFoundException("Отчёт не найден: " + reportId));

        PublishDestination destination = publishDestinationRepository.findById(destinationId)
                .orElseThrow(() -> new NotFoundException("Назначение публикации не найдено: " + destinationId));

        PublishChannel channel = destination.getChannel();

        Publication publication = new Publication();
        publication.setReport(report);
        publication.setDestination(destination);
        publication.setStatus("PUBLISHING");
        publication = publicationRepository.save(publication);

        report.setStatus("PUBLISHING");
        topNReportRepository.save(report);

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
            } else if ("DEMO_PAGE".equals(channel.getCode())) {
                result = demoPagePublisher.publish(reportDto, publication);
            } else if ("WEBHOOK".equals(channel.getCode())) {
                String url = destination.getExternalIdentifier();
                result = webhookPublisher.publish(reportDto, url);
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
            result.setReportId(report.getId());
            result.setCompanyName(report.getCompany().getName());
            result.setReportPeriodStart(report.getPeriodStart());
            result.setReportPeriodEnd(report.getPeriodEnd());
            result.setCreatedAt(publication.getCreatedAt());
            fillPublicationLabels(result, channel, destination);
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
        } else if ("DEMO_PAGE".equals(channel.getCode())) {
            demoPagePublisher.deleteSnapshotForPublication(publication.getId());
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
        result.setReportId(report.getId());
        result.setCompanyName(report.getCompany().getName());
        result.setReportPeriodStart(report.getPeriodStart());
        result.setReportPeriodEnd(report.getPeriodEnd());
        result.setCreatedAt(publication.getCreatedAt());
        result.setStatus("RECALLED");
        result.setExternalId(publication.getExternalId());
        fillPublicationLabels(result, channel, destination);
        return result;
    }

    public List<TopNReportDto> listReports(Long companyId, String status) {
        List<TopNReport> reports;
        if (status != null && !status.isBlank()) {
            reports = topNReportRepository.findByCompany_IdAndStatusOrderByCreatedAtDesc(companyId, status);
        } else {
            reports = topNReportRepository.findByCompany_IdOrderByCreatedAtDesc(companyId);
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
            PublishChannel ch = pub.getDestination().getChannel();
            dto.setChannelId(ch.getId());
            dto.setDestinationId(pub.getDestination().getId());
            dto.setReportId(pub.getReport().getId());
            dto.setCompanyName(pub.getReport().getCompany().getName());
            dto.setReportPeriodStart(pub.getReport().getPeriodStart());
            dto.setReportPeriodEnd(pub.getReport().getPeriodEnd());
            dto.setCreatedAt(pub.getCreatedAt());
            dto.setStatus(pub.getStatus());
            dto.setExternalId(pub.getExternalId());
            fillPublicationLabels(dto, ch, pub.getDestination());
            if ("DEMO_PAGE".equals(ch.getCode()) && StringUtils.hasText(pub.getExternalId())) {
                dto.setViewerPath("/api/public/demo/view/" + pub.getExternalId());
            }
            return dto;
        }).collect(Collectors.toList());
    }

    private static void fillPublicationLabels(PublicationResultDto result, PublishChannel channel, PublishDestination destination) {
        result.setChannelCode(channel.getCode());
        result.setChannelName(channel.getName());
        String label = destination.getLabel();
        result.setDestinationLabel(StringUtils.hasText(label) ? label : channel.getName());
    }

    private static String maskChatId(String raw) {
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        if (raw.length() <= 4) {
            return "••••";
        }
        return "••••" + raw.substring(raw.length() - 4);
    }

    public UserRepository getUserRepository() { return userRepository; }
    public CompanyRepository getCompanyRepository() { return companyRepository; }
    public MoySkladIntegrationRepository getMoyskladIntegrationRepository() { return moyskladIntegrationRepository; }
    public TelegramIntegrationRepository getTelegramIntegrationRepository() { return telegramIntegrationRepository; }
    public TopNReportRepository getTopNReportRepository() { return topNReportRepository; }
    public TopNEntryRepository getTopNEntryRepository() { return topNEntryRepository; }

    private String resolveTelegramToken(TelegramIntegration integration) {
        String headerValue = readDebugHeader("X-Debug-Telegram-Token");
        if (StringUtils.hasText(headerValue)) {
            return headerValue;
        }
        return StringUtils.hasText(telegramTokenOverride)
                ? telegramTokenOverride
                : integration.getBotTokenEncrypted();
    }

    private String resolveTelegramChatId(PublishDestination destination, TelegramIntegration integration) {
        String headerValue = readDebugHeader("X-Debug-Telegram-Chat-Id");
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
        String headerValue = readDebugHeader("X-Debug-Moysklad-Token");
        if (StringUtils.hasText(headerValue)) {
            return headerValue;
        }
        return StringUtils.hasText(moyskladTokenOverride)
                ? moyskladTokenOverride
                : integration.getTokenEncrypted();
    }

    private String readDebugHeader(String headerName) {
        if (!allowDebugHeaders) {
            return null;
        }
        var attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes servletAttrs) {
            return servletAttrs.getRequest().getHeader(headerName);
        }
        return null;
    }
}
