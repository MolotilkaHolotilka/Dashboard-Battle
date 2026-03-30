package ru.dashboardbattle.integration.real;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import ru.dashboardbattle.dto.PublicationResultDto;
import ru.dashboardbattle.dto.TopNEntryDto;
import ru.dashboardbattle.dto.TopNReportDto;
import ru.dashboardbattle.exception.IntegrationException;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class TelegramPublisherHttpTest {

    @Test
    void publish_shouldReturnExternalIdFromTelegramResponse() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        TelegramPublisherHttp publisher = new TelegramPublisherHttp(restTemplate);

        server.expect(requestTo("https://api.telegram.org/botbot-token/sendMessage"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("""
                        {"ok": true, "result": {"message_id": 12345}}
                        """, MediaType.APPLICATION_JSON));

        PublicationResultDto result = publisher.publish(sampleReport(), "bot-token", "-1001");

        assertThat(result.getStatus()).isEqualTo("PUBLISHED");
        assertThat(result.getExternalId()).isEqualTo("12345");
        server.verify();
    }

    @Test
    void cancelPublication_shouldThrowWhenExternalIdIsInvalid() {
        TelegramPublisherHttp publisher = new TelegramPublisherHttp(new RestTemplate());

        assertThatThrownBy(() -> publisher.cancelPublication("token", "-1001", "tg-msg-1"))
                .isInstanceOf(IntegrationException.class)
                .hasMessageContaining("Некорректный externalId");
    }

    private TopNReportDto sampleReport() {
        TopNReportDto dto = new TopNReportDto();
        dto.setStatus("PENDING");
        TopNEntryDto entry = new TopNEntryDto();
        entry.setRank(1);
        entry.setEmployeeName("Ivan");
        entry.setRevenue(new BigDecimal("1000"));
        entry.setMargin(new BigDecimal("200"));
        entry.setFavoriteProduct("Product");
        dto.setEntries(List.of(entry));
        return dto;
    }
}
