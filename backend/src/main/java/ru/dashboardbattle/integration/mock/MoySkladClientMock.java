package ru.dashboardbattle.integration.mock;

import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import ru.dashboardbattle.dto.TopNEntryDto;
import ru.dashboardbattle.dto.TopNReportDto;
import ru.dashboardbattle.integration.MoySkladClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// мок МойСклад — возвращает захардкоженные данные
@Component
@ConditionalOnProperty(name = "integration.moysklad.real-enabled", havingValue = "false", matchIfMissing = true)
public class MoySkladClientMock implements MoySkladClient {

    @Override
    public TopNReportDto fetchTopN(String token, int topN) {
        TopNReportDto report = new TopNReportDto();
        report.setPeriodStart(LocalDate.now().minusDays(30));
        report.setPeriodEnd(LocalDate.now());
        report.setStatus("PENDING");

        // Все возможные записи в порядке убывания рейтинга
        List<TopNEntryDto> all = new ArrayList<>();
        all.add(new TopNEntryDto(
                "Иванов Иван", "ms-emp-001",
                new BigDecimal("1500000.00"), new BigDecimal("350000.00"),
                "Ноутбук ASUS VivoBook", 1));
        all.add(new TopNEntryDto(
                "Петрова Мария", "ms-emp-002",
                new BigDecimal("1200000.00"), new BigDecimal("280000.00"),
                "Монитор Samsung 27\"", 2));
        all.add(new TopNEntryDto(
                "Сидоров Алексей", "ms-emp-003",
                new BigDecimal("980000.00"), new BigDecimal("210000.00"),
                "Клавиатура Logitech MX", 3));
        all.add(new TopNEntryDto(
                "Козлова Анна", "ms-emp-004",
                new BigDecimal("870000.00"), new BigDecimal("195000.00"),
                "Мышь Razer DeathAdder", 4));
        all.add(new TopNEntryDto(
                "Новиков Дмитрий", "ms-emp-005",
                new BigDecimal("750000.00"), new BigDecimal("160000.00"),
                "Наушники Sony WH-1000XM5", 5));

        // Обрезаем строго до topN записей
        int limit = Math.min(Math.max(topN, 1), all.size());
        List<TopNEntryDto> entries = all.subList(0, limit);

        report.setEntries(new ArrayList<>(entries));
        return report;
    }
}
