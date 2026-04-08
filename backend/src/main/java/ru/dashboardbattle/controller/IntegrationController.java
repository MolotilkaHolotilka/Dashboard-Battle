package ru.dashboardbattle.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.dashboardbattle.dto.IntegrationDataDto;
import ru.dashboardbattle.dto.MoyskladIntegrationUpsertRequestDto;
import ru.dashboardbattle.dto.TelegramIntegrationUpsertRequestDto;
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

    @PostMapping("/moysklad")
    public ResponseEntity<IntegrationDataDto> upsertMoysklad(@RequestBody MoyskladIntegrationUpsertRequestDto body) {
        return ResponseEntity.ok(service.upsertMoyskladIntegration(body));
    }

    @PostMapping("/telegram")
    public ResponseEntity<IntegrationDataDto> upsertTelegram(@RequestBody TelegramIntegrationUpsertRequestDto body) {
        return ResponseEntity.ok(service.upsertTelegramIntegration(body));
    }
}
