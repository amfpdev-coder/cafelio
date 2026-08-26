package com.cafelio.api.service;

import com.cafelio.api.client.OpenLibraryClient;
import com.cafelio.api.dto.response.OpenLibrarySearchResponse;
import org.springframework.stereotype.Service;

@Service
public class BookSearchService {

    private final OpenLibraryClient openLibraryClient;

    public BookSearchService(OpenLibraryClient openLibraryClient) {
        this.openLibraryClient = openLibraryClient;
    }

    public OpenLibrarySearchResponse search(String titulo, int page, int limit) {
        return openLibraryClient.searchBooks(titulo, page, limit);
    }
}
