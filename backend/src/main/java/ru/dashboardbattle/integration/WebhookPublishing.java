package ru.dashboardbattle.integration;

import ru.dashboardbattle.dto.PublicationResultDto;
import ru.dashboardbattle.dto.TopNReportDto;

public interface WebhookPublishing {

    PublicationResultDto publish(TopNReportDto reportDto, String callbackUrl);
}
