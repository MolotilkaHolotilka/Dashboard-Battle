package ru.dashboardbattle.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.dashboardbattle.dto.LoginRequestDto;
import ru.dashboardbattle.dto.LoginResponseDto;
import ru.dashboardbattle.dto.RegistrationRequestDto;
import ru.dashboardbattle.dto.RegistrationResponseDto;
import ru.dashboardbattle.service.DashboardBattleService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final DashboardBattleService service;

    public AuthController(DashboardBattleService service) {
        this.service = service;
    }

    @PostMapping("/register")
    public ResponseEntity<RegistrationResponseDto> register(@RequestBody RegistrationRequestDto request) {
        RegistrationResponseDto response = service.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto request) {
        LoginResponseDto response = service.login(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(response);
    }
}
