package ru.dashboardbattle.integration;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.dashboardbattle.dto.PublicationResultDto;
import ru.dashboardbattle.dto.TopNReportDto;
import ru.dashboardbattle.entity.DemoPageSnapshot;
import ru.dashboardbattle.entity.Publication;
import ru.dashboardbattle.repository.DemoPageSnapshotRepository;

import java.util.UUID;

@Service
public class DemoPagePublisher implements DemoPagePublishing {

    private final DemoPageSnapshotRepository snapshotRepository;

    public DemoPagePublisher(DemoPageSnapshotRepository snapshotRepository) {
        this.snapshotRepository = snapshotRepository;
    }

    @Transactional
    public PublicationResultDto publish(TopNReportDto reportDto, Publication publication) {
        String token = UUID.randomUUID().toString();
        String html = DemoPageHtmlBuilder.build(reportDto);

        DemoPageSnapshot snap = new DemoPageSnapshot();
        snap.setPublication(publication);
        snap.setShareToken(token);
        snap.setHtmlBody(html);
        snapshotRepository.save(snap);

        PublicationResultDto result = new PublicationResultDto();
        result.setStatus("PUBLISHED");
        result.setExternalId(token);
        result.setViewerPath("/api/public/demo/view/" + token);
        return result;
    }

    @Transactional
    public void deleteSnapshotForPublication(Long publicationId) {
        snapshotRepository.deleteByPublicationId(publicationId);
    }
}
