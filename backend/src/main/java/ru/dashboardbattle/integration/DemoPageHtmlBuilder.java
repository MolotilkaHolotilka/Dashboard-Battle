package ru.dashboardbattle.integration;

import ru.dashboardbattle.dto.TopNEntryDto;
import ru.dashboardbattle.dto.TopNReportDto;

import java.util.List;

public final class DemoPageHtmlBuilder {

    private DemoPageHtmlBuilder() {
    }

    public static String build(TopNReportDto report) {
        StringBuilder rows = new StringBuilder();
        List<TopNEntryDto> entries = report.getEntries();
        if (entries != null) {
            for (TopNEntryDto e : entries) {
                rows.append("<tr>")
                        .append("<td>").append(esc(e.getRank())).append("</td>")
                        .append("<td>").append(esc(e.getEmployeeName())).append("</td>")
                        .append("<td class=\"num\">").append(esc(String.valueOf(e.getRevenue()))).append("</td>")
                        .append("<td class=\"num\">").append(esc(String.valueOf(e.getMargin()))).append("</td>")
                        .append("<td>").append(esc(e.getFavoriteProduct())).append("</td>")
                        .append("</tr>\n");
            }
        }

        return """
                <!DOCTYPE html>
                <html lang="ru">
                <head>
                  <meta charset="utf-8"/>
                  <meta name="viewport" content="width=device-width, initial-scale=1"/>
                  <title>ТОП продавцов — демо</title>
                  <style>
                    body { font-family: system-ui, sans-serif; margin: 2rem; background: #0f1419; color: #e6edf3; }
                    h1 { font-weight: 600; }
                    .meta { opacity: .85; margin-bottom: 1.5rem; font-size: .95rem; }
                    table { border-collapse: collapse; width: 100%%; max-width: 960px; }
                    th, td { border: 1px solid #30363d; padding: .6rem .75rem; text-align: left; }
                    th { background: #161b22; }
                    tr:nth-child(even) { background: #131820; }
                    .num { text-align: right; font-variant-numeric: tabular-nums; }
                    footer { margin-top: 2rem; font-size: .8rem; opacity: .6; }
                  </style>
                </head>
                <body>
                  <h1>Рейтинг продавцов (ТОП-N)</h1>
                  <div class="meta">
                    Период: %s — %s · Компания (id): %s · Отчёт #%s
                  </div>
                  <table>
                    <thead><tr><th>#</th><th>Сотрудник</th><th>Выручка</th><th>Маржа</th><th>Топ‑продукт</th></tr></thead>
                    <tbody>
                    %s
                    </tbody>
                  </table>
                  <footer>Сгенерировано «Битва Дашбордов» (канал «Демо-страница»)</footer>
                </body>
                </html>
                """.formatted(
                esc(String.valueOf(report.getPeriodStart())),
                esc(String.valueOf(report.getPeriodEnd())),
                esc(String.valueOf(report.getCompanyId())),
                esc(String.valueOf(report.getId())),
                rows.toString()
        );
    }

    private static String esc(Object raw) {
        if (raw == null) {
            return "";
        }
        String s = String.valueOf(raw);
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
