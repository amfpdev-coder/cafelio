package com.cafelio.api.controller;

import com.cafelio.api.dto.response.BookSearchResponse;
import com.cafelio.api.service.BookSearchService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/books")
@Validated
public class BookController {

    private final BookSearchService bookSearchService;

    public BookController(BookSearchService bookSearchService) {
        this.bookSearchService = bookSearchService;
    }

    @GetMapping("/search")
    public ResponseEntity<BookSearchResponse> search(
            @RequestParam
            @NotBlank(message = "O título é obrigatório")
            String titulo,

            @RequestParam(defaultValue = "1")
            @Min(value = 1, message = "A página deve ser no mínimo 1")
            int page,

            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "O limite deve ser no mínimo 1")
            @Max(value = 50, message = "O limite deve ser no máximo 50")
            int limit
    ) {
        return ResponseEntity.ok(
                bookSearchService.search(titulo, page, limit)
        );
    }
}

