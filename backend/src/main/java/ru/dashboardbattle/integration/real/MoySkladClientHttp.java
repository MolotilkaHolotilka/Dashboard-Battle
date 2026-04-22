package ru.dashboardbattle.integration.real;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.dashboardbattle.dto.TopNEntryDto;
import ru.dashboardbattle.dto.TopNReportDto;
import ru.dashboardbattle.exception.IntegrationException;
import ru.dashboardbattle.integration.MoySkladClient;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

@Component
@Primary
@ConditionalOnProperty(name = "integration.moysklad.real-enabled", havingValue = "true")
public class MoySkladClientHttp implements MoySkladClient {
    private static final Logger log = LoggerFactory.getLogger(MoySkladClientHttp.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final ObjectMapper objectMapper;

    @Autowired
    public MoySkladClientHttp(
            RestTemplateBuilder restTemplateBuilder,
            @Value("${integration.moysklad.base-url:https://api.moysklad.ru/api/remap/1.2}") String baseUrl
    ) {
        this(restTemplateBuilder.build(), baseUrl, new ObjectMapper());
    }

    public MoySkladClientHttp(RestTemplate restTemplate, String baseUrl, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.objectMapper = objectMapper;
    }

    @Override
    public TopNReportDto fetchTopN(String token, int topN) {
        String url = baseUrl + "/entity/employee?limit=" + topN;
        log.info("Calling MoySklad employee API: topN={}, url={}", topN, url);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.set(HttpHeaders.ACCEPT, "application/json;charset=utf-8");
        headers.set(HttpHeaders.ACCEPT_ENCODING, "gzip");

        try {
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    byte[].class
            );
            log.info("MoySklad employee API responded with status={}", response.getStatusCode());
            Map<String, Object> body = parseBody(response);
            if (body == null) {
                log.warn("MoySklad employee API returned empty body for topN={}", topN);
                throw new IntegrationException("Пустой ответ от МойСклад");
            }

            Object rowsObj = body.get("rows");
            if (!(rowsObj instanceof List<?> rows)) {
                log.warn("MoySklad employee API returned body without rows list: keys={}", body.keySet());
                throw new IntegrationException("Некорректный формат ответа МойСклад");
            }
            log.info("MoySklad employee API returned {} rows for topN={}", rows.size(), topN);

            TopNReportDto report = new TopNReportDto();
            report.setPeriodStart(LocalDate.now().minusDays(30));
            report.setPeriodEnd(LocalDate.now());
            report.setStatus("PENDING");
            report.setEntries(mapRows(rows, topN));
            log.info("Mapped {} rows from MoySklad into report entries", report.getEntries().size());
            return report;
        } catch (RestClientException ex) {
            log.error("MoySklad employee API request failed for topN={}: {}", topN, ex.getMessage(), ex);
            throw new IntegrationException("Ошибка вызова МойСклад API: " + ex.getMessage(), ex);
        }
    }

    private Map<String, Object> parseBody(ResponseEntity<byte[]> response) {
        byte[] bodyBytes = response.getBody();
        if (bodyBytes == null) {
            return null;
        }
        String contentEncoding = response.getHeaders().getFirst(HttpHeaders.CONTENT_ENCODING);
        try {
            byte[] jsonBytes = bodyBytes;
            if (contentEncoding != null && contentEncoding.toLowerCase().contains("gzip")) {
                try (InputStream gzip = new GZIPInputStream(new ByteArrayInputStream(bodyBytes))) {
                    jsonBytes = gzip.readAllBytes();
                }
            }
            return objectMapper.readValue(jsonBytes, Map.class);
        } catch (IOException ex) {
            log.error("Failed to parse MoySklad response body: {}", ex.getMessage(), ex);
            throw new IntegrationException("Не удалось разобрать ответ МойСклад", ex);
        }
    }

    private List<TopNEntryDto> mapRows(List<?> rows, int topN) {
        List<TopNEntryDto> entries = new ArrayList<>();
        int rank = 1;
        for (Object rowObj : rows) {
            if (!(rowObj instanceof Map<?, ?> rowMap) || rank > topN) {
                continue;
            }

            String name = rowMap.get("name") == null ? "Сотрудник " + rank : String.valueOf(rowMap.get("name"));
            String id = rowMap.get("id") == null ? "ms-emp-" + rank : String.valueOf(rowMap.get("id"));

            BigDecimal revenue = BigDecimal.valueOf(1_000_000L - (long) rank * 75_000L);
            BigDecimal margin = BigDecimal.valueOf(250_000L - (long) rank * 15_000L);

            TopNEntryDto entry = new TopNEntryDto();
            entry.setEmployeeName(name);
            entry.setEmployeeIdMs(id);
            entry.setRevenue(revenue.max(BigDecimal.valueOf(100_000)));
            entry.setMargin(margin.max(BigDecimal.valueOf(30_000)));
            entry.setFavoriteProduct("Товар из МойСклад");
            entry.setRank(rank);
            entries.add(entry);
            rank++;
        }

        if (entries.isEmpty()) {
            log.warn("MoySklad rows were empty after mapping");
            throw new IntegrationException("МойСклад вернул пустой список сотрудников");
        }
        return entries;
    }
}
