package ru.dashboardbattle.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.dashboardbattle.dto.CompanySummaryDto;
import ru.dashboardbattle.service.DashboardBattleService;

import java.util.List;
import java.util.Map;

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

    @GetMapping
    public ResponseEntity<List<CompanySummaryDto>> listCompaniesByUser(@RequestParam Long userId) {
        return ResponseEntity.ok(service.listCompaniesByUser(userId));
    }

    @PostMapping
    public ResponseEntity<CompanySummaryDto> createCompany(@RequestBody Map<String, Object> body) {
        Long userId = body.containsKey("userId") ? Long.valueOf(body.get("userId").toString()) : null;
        String companyName = body.containsKey("companyName") ? body.get("companyName").toString() : null;
        return ResponseEntity.ok(service.createCompany(userId, companyName));
    }
}
