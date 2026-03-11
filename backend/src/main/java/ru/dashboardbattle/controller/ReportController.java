package ru.dashboardbattle.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getReport(@PathVariable Long id) {
        return ResponseEntity.ok(Map.of(
                "id", id,
                "status", "DRAFT"
        ));
    }
}
