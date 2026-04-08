package ru.dashboardbattle.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.dashboardbattle.entity.DemoPageSnapshot;
import ru.dashboardbattle.exception.NotFoundException;
import ru.dashboardbattle.repository.DemoPageSnapshotRepository;

@RestController
public class DemoPageViewController {

    private final DemoPageSnapshotRepository snapshotRepository;

    public DemoPageViewController(DemoPageSnapshotRepository snapshotRepository) {
        this.snapshotRepository = snapshotRepository;
    }

    @GetMapping(value = "/api/public/demo/view/{token}", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> viewDemoPage(@PathVariable String token) {
        DemoPageSnapshot snap = snapshotRepository.findByShareToken(token)
                .orElseThrow(() -> new NotFoundException("Демо-страница не найдена или ссылка устарела."));
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(snap.getHtmlBody());
    }
}
