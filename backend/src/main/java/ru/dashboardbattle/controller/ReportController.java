package ru.dashboardbattle.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.dashboardbattle.dto.PublicationRequestDto;
import ru.dashboardbattle.dto.PublicationResultDto;
import ru.dashboardbattle.dto.TopNReportDto;
import ru.dashboardbattle.service.DashboardBattleService;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final DashboardBattleService service;

    public ReportController(DashboardBattleService service) {
        this.service = service;
    }

    @PostMapping("/top-n/request/{companyId}")
    public ResponseEntity<TopNReportDto> requestTopN(
            @PathVariable Long companyId,
            @RequestParam(defaultValue = "5") int topN) {
        TopNReportDto result = service.requestTopN(companyId, topN);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/top-n/{id}/confirm")
    public ResponseEntity<TopNReportDto> confirmTopN(@PathVariable Long id) {
        TopNReportDto result = service.confirmTopN(id);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/top-n/{id}/publish")
    public ResponseEntity<PublicationResultDto> publishTopN(
            @PathVariable Long id,
            @RequestBody PublicationRequestDto request) {
        PublicationResultDto result = service.publishTopN(id, request.getDestinationId());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/top-n")
    public ResponseEntity<List<TopNReportDto>> listReports(
            @RequestParam Long companyId,
            @RequestParam(required = false) String status) {
        List<TopNReportDto> reports = service.listReports(companyId, status);
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TopNReportDto> getReport(@PathVariable Long id) {
        TopNReportDto dto = service.confirmTopN(id);
        return ResponseEntity.ok(dto);
    }
}
