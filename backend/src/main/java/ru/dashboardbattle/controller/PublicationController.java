package ru.dashboardbattle.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.dashboardbattle.dto.PublicationResultDto;
import ru.dashboardbattle.service.DashboardBattleService;

import java.util.List;

@RestController
@RequestMapping("/api/publications")
public class PublicationController {

    private final DashboardBattleService service;

    public PublicationController(DashboardBattleService service) {
        this.service = service;
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<PublicationResultDto> cancelPublication(@PathVariable Long id) {
        PublicationResultDto result = service.cancelPublication(id);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<List<PublicationResultDto>> listPublications(
            @RequestParam Long companyId,
            @RequestParam(required = false) Long channelId,
            @RequestParam(required = false) Long destinationId) {
        List<PublicationResultDto> publications = service.listPublications(companyId, channelId, destinationId);
        return ResponseEntity.ok(publications);
    }
}
