package com.cafelio.api.controller;

import com.cafelio.api.dto.request.LibraryBookCreateRequest;
import com.cafelio.api.dto.response.LibraryBookResponse;
import com.cafelio.api.service.LibraryBookService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/library-books")
public class LibraryBookController {

    private final LibraryBookService libraryBookService;

    public LibraryBookController(LibraryBookService libraryBookService){
        this.libraryBookService = libraryBookService;
    };

    @PostMapping
    public LibraryBookResponse createLibraryBook(@Valid @RequestBody LibraryBookCreateRequest libraryBookCreateRequest, Authentication authentication){
        Object principal = authentication.getPrincipal();
        UUID userId = (UUID) principal;
        return libraryBookService.createLibraryBook(libraryBookCreateRequest, userId);
    }

    @GetMapping
    public List<LibraryBookResponse> listBooks(Authentication authentication){
        Object principal = authentication.getPrincipal();
        UUID userId = (UUID) principal;
        return libraryBookService.listBooks(userId);
    }
}
