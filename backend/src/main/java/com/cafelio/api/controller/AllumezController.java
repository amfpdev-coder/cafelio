package com.cafelio.api.controller;

import com.cafelio.api.dto.AllumezResponse;
import com.cafelio.api.dto.AnnualGoalRequest;
import com.cafelio.api.service.AllumezService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/challenges/allumez")
@SecurityRequirement(name = "bearerAuth")
public class AllumezController {

    private final AllumezService allumezService;

    public AllumezController(AllumezService allumezService) {
        this.allumezService = allumezService;
    }

    @GetMapping
    public ResponseEntity<AllumezResponse> consultar(@AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(allumezService.consultar(userId));
    }

    @PutMapping("/goal")
    public ResponseEntity<AllumezResponse> definirMeta(
            @AuthenticationPrincipal UUID userId,
            @RequestBody AnnualGoalRequest request
    ) {
        return ResponseEntity.ok(allumezService.definirMeta(userId, request.quantidade()));
    }
}
