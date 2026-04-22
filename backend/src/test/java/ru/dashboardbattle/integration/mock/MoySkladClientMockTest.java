package ru.dashboardbattle.integration.mock;

import org.junit.jupiter.api.Test;
import ru.dashboardbattle.dto.TopNReportDto;

import static org.assertj.core.api.Assertions.assertThat;

class MoySkladClientMockTest {

    private final MoySkladClientMock client = new MoySkladClientMock();

    @Test
    void fetchTopN_shouldReturnExactlyRequestedNumberOfEntries() {
        TopNReportDto report = client.fetchTopN("token", 2);

        assertThat(report.getEntries()).hasSize(2);
        assertThat(report.getEntries())
                .extracting(entry -> entry.getRank())
                .containsExactly(1, 2);
    }
}
