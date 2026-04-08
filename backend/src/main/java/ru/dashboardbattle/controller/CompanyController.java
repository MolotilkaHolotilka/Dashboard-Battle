package ru.dashboardbattle.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.dashboardbattle.dto.CompanySummaryDto;
import ru.dashboardbattle.service.DashboardBattleService;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    private final DashboardBattleService service;

    public CompanyController(DashboardBattleService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompanySummaryDto> getCompany(@PathVariable Long id) {
        return ResponseEntity.ok(service.getCompanySummary(id));
    }
}
