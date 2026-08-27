package com.cafelio.api.service;

import com.cafelio.api.client.OpenLibraryClient;
import com.cafelio.api.dto.response.BookResponse;
import com.cafelio.api.dto.response.BookSearchResponse;
import com.cafelio.api.dto.response.OpenLibraryBookResponse;
import com.cafelio.api.dto.response.OpenLibrarySearchResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookSearchServiceTest {
    @Mock
    private OpenLibraryClient openLibraryClient;

    @InjectMocks
    private BookSearchService bookSearchService;

    @Test
    void shouldReturnNullCoverUrlWhenBookHasNoCover() {
        OpenLibraryBookResponse book = new OpenLibraryBookResponse(
                "/works/OL123W",
                "Livro sem capa",
                List.of("Autor Teste"),
                2000,
                null
        );

        OpenLibrarySearchResponse openLibraryResponse =
                new OpenLibrarySearchResponse(
                        1,
                        0,
                        true,
                        List.of(book)
                );

        when(
                openLibraryClient.searchBooks(
                        "Livro sem capa",
                        1,
                        10
                )
        ).thenReturn(openLibraryResponse);

        BookSearchResponse result =
                bookSearchService.search(
                        "Livro sem capa",
                        1,
                        10
                );

        BookResponse resultBook =
                result.books().get(0);

        assertNull(resultBook.coverUrl());
        assertEquals("OL123W", resultBook.openLibraryId());
        assertEquals("Livro sem capa", resultBook.title());
    }
}