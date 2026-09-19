package com.cafelio.api.controller;

import com.cafelio.api.dto.request.LibraryBookCreateRequest;
import com.cafelio.api.dto.response.LibraryBookResponse;
import com.cafelio.api.service.LibraryBookService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/library-books")
public class LibraryBookController {

    private final LibraryBookService libraryBookService;

    public LibraryBookController(LibraryBookService libraryBookService){
        this.libraryBookService = libraryBookService;
    };

    @PostMapping
    public LibraryBookResponse createLibraryBook(@Valid @RequestBody LibraryBookCreateRequest libraryBookCreateRequest){
        return libraryBookService.createLibraryBook(libraryBookCreateRequest);
    }
}
