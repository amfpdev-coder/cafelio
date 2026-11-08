package com.cafelio.api.service;

import com.cafelio.api.client.GoogleBooksClient;
import com.cafelio.api.dto.response.BookResponse;
import com.cafelio.api.dto.response.BookSearchResponse;
import com.cafelio.api.dto.response.GoogleBooksSearchResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookSearchService {

    private final GoogleBooksClient googleBooksClient;

    public BookSearchService(GoogleBooksClient googleBooksClient) {
        this.googleBooksClient = googleBooksClient;
    }

    public BookSearchResponse search(String titulo, int page, int limit) {
        GoogleBooksSearchResponse response = googleBooksClient.searchBooks(titulo, page, limit);

        List<BookResponse> books = (response.items() != null)
                ? response.items().stream()
                  .filter(this::isValidBook) // <-- Nossos filtros de qualidade entram aqui!
                  .map(this::toBookResponse)
                  .toList()
                : List.of();

        return new BookSearchResponse(
                response.totalItems(),
                page,
                limit,
                books
        );
    }

    private boolean isValidBook(GoogleBooksSearchResponse.GoogleBookItem item) {
        if (item.volumeInfo() == null) return false;
        var info = item.volumeInfo();

        // 1. Validar se tem título
        if (info.title() == null || info.title().isBlank()) {
            return false;
        }

        // 2. Filtrar termos proibidos no título (convertendo para minúsculas para comparar)
        String titleLower = info.title().toLowerCase();
        if (titleLower.contains("resumo de") ||
                titleLower.contains("guia de estudo") ||
                titleLower.contains("study guide") ||
                titleLower.contains("sparknotes")) {
            return false;
        }

        // 3. Exigir capa (imageLinks e thumbnail válidos)
        if (info.imageLinks() == null || info.imageLinks().thumbnail() == null || info.imageLinks().thumbnail().isBlank()) {
            return false;
        }

        // 4. Exigir número mínimo de páginas (ex: 50 páginas)
        if (info.pageCount() == null || info.pageCount() < 50) {
            return false;
        }

        // 5. Exigir autores
        if (info.authors() == null || info.authors().isEmpty()) {
            return false;
        }

        return true;
    }

    private BookResponse toBookResponse(GoogleBooksSearchResponse.GoogleBookItem item) {
        var info = item.volumeInfo();

        List<String> authors = info.authors();

        String coverUrl = info.imageLinks().thumbnail();
        if (coverUrl != null) {
            coverUrl = coverUrl.replace("http://", "https://");
        }

        Integer year = null;
        if (info.publishedDate() != null && info.publishedDate().length() >= 4) {
            try {
                year = Integer.parseInt(info.publishedDate().substring(0, 4));
            } catch (NumberFormatException e) {
                year = null;
            }
        }

        return new BookResponse(
                item.id(),
                info.title(),
                info.language(),
                authors,
                year,
                coverUrl,
                info.pageCount()
        );
    }
}