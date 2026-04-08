package ru.dashboardbattle.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.dashboardbattle.dto.CreatePublishDestinationRequestDto;
import ru.dashboardbattle.dto.PublishChannelDto;
import ru.dashboardbattle.dto.PublishDestinationDto;
import ru.dashboardbattle.service.DashboardBattleService;

import java.util.List;

@RestController
@RequestMapping("/api/publish")
public class PublishSetupController {

    private final DashboardBattleService service;

    public PublishSetupController(DashboardBattleService service) {
        this.service = service;
    }

    @GetMapping("/channels")
    public ResponseEntity<List<PublishChannelDto>> listChannels() {
        return ResponseEntity.ok(service.listPublishChannels());
    }

    @GetMapping("/destinations")
    public ResponseEntity<List<PublishDestinationDto>> listDestinations(@RequestParam Long companyId) {
        return ResponseEntity.ok(service.listPublishDestinations(companyId));
    }

    @PostMapping("/destinations")
    public ResponseEntity<PublishDestinationDto> createDestination(@RequestBody CreatePublishDestinationRequestDto body) {
        return ResponseEntity.ok(service.createPublishDestination(body));
    }
}
