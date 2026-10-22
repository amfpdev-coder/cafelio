package com.cafelio.api.controller;

import com.cafelio.api.dto.HomeResponse;
import com.cafelio.api.service.HomeService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/home")
@SecurityRequirement(name = "bearerAuth")
public class HomeController {

    private final HomeService homeService;

    public HomeController(HomeService homeService) {
        this.homeService = homeService;
    }

    @GetMapping
    public ResponseEntity<HomeResponse> consultar(@AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(homeService.consultar(userId));
    }
}
