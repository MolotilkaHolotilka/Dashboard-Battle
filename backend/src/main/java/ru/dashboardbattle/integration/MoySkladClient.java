package ru.dashboardbattle.integration;

import ru.dashboardbattle.dto.TopNReportDto;


// клиент МойСклад (пока мок)
public interface MoySkladClient {

    // получить топ-N продавцов
    TopNReportDto fetchTopN(String token, int topN);
}
