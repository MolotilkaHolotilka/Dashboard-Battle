package ru.dashboardbattle.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.dashboardbattle.dto.IntegrationDataDto;
import ru.dashboardbattle.service.DashboardBattleService;

@RestController
@RequestMapping("/api/integrations")
public class IntegrationController {

    private final DashboardBattleService service;

    public IntegrationController(DashboardBattleService service) {
        this.service = service;
    }

    @GetMapping("/{companyId}")
    public ResponseEntity<IntegrationDataDto> getIntegrationData(@PathVariable Long companyId) {
        IntegrationDataDto data = service.getIntegrationData(companyId);
        return ResponseEntity.ok(data);
    }
}
