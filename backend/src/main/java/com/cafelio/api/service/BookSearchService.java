package com.cafelio.api.service;

import com.cafelio.api.client.OpenLibraryClient;
import com.cafelio.api.dto.response.BookResponse;
import com.cafelio.api.dto.response.BookSearchResponse;
import com.cafelio.api.dto.response.OpenLibraryBookResponse;
import com.cafelio.api.dto.response.OpenLibrarySearchResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookSearchService {

    private final OpenLibraryClient openLibraryClient;

    public BookSearchService(OpenLibraryClient openLibraryClient) {
        this.openLibraryClient = openLibraryClient;
    }

    public BookSearchResponse search(String titulo, int page, int limit) {
        OpenLibrarySearchResponse response = openLibraryClient.searchBooks(titulo, page, limit);
        List<BookResponse> books = response.docs()
                .stream()
                .map(this::toBookResponse)
                .toList();

        return new BookSearchResponse(
                response.totalResults(),
                page,
                limit,
                books
        );
    }

    private BookResponse toBookResponse(OpenLibrarySearchResponse book) {
        return null;
    }

    private BookResponse toBookResponse(OpenLibraryBookResponse book){

        String openLibraryId = book.key().replace("/works/", "");

        String coverUrl = null;

        if(book.coverId() != null){
            coverUrl = "https://covers.openlibrary.org/b/id/" + book.coverId() + "-M.jpg";
        }

        return new BookResponse(
                openLibraryId,
                book.title(),
                book.authors(),
                book.firstPublishYear(),
                coverUrl
        );
    }
}
