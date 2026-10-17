package com.cafelio.api.controller;

import com.cafelio.api.dto.FlammeResponse;
import com.cafelio.api.service.FlammeService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/challenges/flamme")
@SecurityRequirement(name = "bearerAuth")
public class FlammeController {

    private final FlammeService flammeService;

    public FlammeController(FlammeService flammeService) {
        this.flammeService = flammeService;
    }

    @GetMapping
    public ResponseEntity<FlammeResponse> consultar(@AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(flammeService.consultar(userId));
    }

    @PostMapping("/today")
    public ResponseEntity<FlammeResponse> marcarHoje(@AuthenticationPrincipal UUID userId) {
        flammeService.marcarHoje(userId);
        return ResponseEntity.ok(flammeService.consultar(userId));
    }
}