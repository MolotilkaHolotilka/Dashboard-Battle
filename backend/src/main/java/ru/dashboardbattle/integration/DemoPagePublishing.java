package ru.dashboardbattle.integration;

import ru.dashboardbattle.dto.PublicationResultDto;
import ru.dashboardbattle.dto.TopNReportDto;
import ru.dashboardbattle.entity.Publication;

public interface DemoPagePublishing {

    PublicationResultDto publish(TopNReportDto reportDto, Publication publication);

    void deleteSnapshotForPublication(Long publicationId);
}
