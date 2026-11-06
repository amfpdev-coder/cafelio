package com.cafelio.api.service;

import com.cafelio.api.client.GoogleBooksClient;
import com.cafelio.api.dto.response.BookResponse;
import com.cafelio.api.dto.response.BookSearchResponse;
import com.cafelio.api.dto.response.GoogleBooksSearchResponse;
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
    private GoogleBooksClient googleBooksClient;

    @InjectMocks
    private BookSearchService bookSearchService;

    @Test
    void shouldReturnNullCoverUrlWhenBookHasNoCover() {
        GoogleBooksSearchResponse.VolumeInfo volumeInfo = new GoogleBooksSearchResponse.VolumeInfo(
                "Livro sem capa",
                "pt",
                List.of("Autor Teste"),
                "2000",
                320,
                null // sem imagem de capa
        );

        GoogleBooksSearchResponse.GoogleBookItem bookItem = new GoogleBooksSearchResponse.GoogleBookItem(
                "google-id-123",
                volumeInfo
        );

        GoogleBooksSearchResponse googleResponse = new GoogleBooksSearchResponse(
                1,
                List.of(bookItem)
        );

        when(
                googleBooksClient.searchBooks(
                        "Livro sem capa",
                        1,
                        10
                )
        ).thenReturn(googleResponse);

        BookSearchResponse result =
                bookSearchService.search(
                        "Livro sem capa",
                        1,
                        10
                );

        BookResponse resultBook = result.books().get(0);

        assertNull(resultBook.coverUrl());
        assertEquals("google-id-123", resultBook.googleBooksId());
        assertEquals("Livro sem capa", resultBook.title());
    }
}