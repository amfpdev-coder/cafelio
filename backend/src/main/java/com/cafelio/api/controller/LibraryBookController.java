package com.cafelio.api.controller;

import com.cafelio.api.dto.request.LibraryBookCreateRequest;
import com.cafelio.api.dto.request.LibraryBookUpdateRequest;
import com.cafelio.api.dto.response.LibraryBookResponse;
import com.cafelio.api.service.LibraryBookService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
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
    };

    @PatchMapping("/{id}")
    public LibraryBookResponse updateBook(@PathVariable UUID id, Authentication authentication, @RequestBody LibraryBookUpdateRequest libraryBookUpdateRequest){
        Object principal = authentication.getPrincipal();
        UUID userId = (UUID) principal;
        return libraryBookService.updateBook(id, userId, libraryBookUpdateRequest);
    }

    @GetMapping("/{id}")
    public LibraryBookResponse getById(@PathVariable UUID id, Authentication authentication){
        Object principal = authentication.getPrincipal();
        UUID userId = (UUID) principal;
        return libraryBookService.getById(id, userId);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBook(@PathVariable UUID id, Authentication authentication){
        Object principal = authentication.getPrincipal();
        UUID userId = (UUID) principal;
        libraryBookService.deleteBook(id, userId);
        return ResponseEntity.ok ("Livro deletado com sucesso");

    }
}
