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

    private BookResponse toBookResponse(GoogleBooksSearchResponse.GoogleBookItem item) {
        var info = item.volumeInfo();

        // Tratamento para autores (o Google retorna uma lista de strings, vamos juntar com vírgula ou pegar padrão)
        List<String> authors = (info.authors() != null && !info.authors().isEmpty())
                ? info.authors()
                : List.of("Autor desconhecido");

        // Tratamento da imagem de capa, se existir nos links do Google
        String coverUrl = null;
        if (info.imageLinks() != null) {
            coverUrl = info.imageLinks().thumbnail();
            // Opcional: o Google costuma mandar 'http://', se quiser garantir HTTPS:
            if (coverUrl != null) {
                coverUrl = coverUrl.replace("http://", "https://");
            }
        }

        // Tratamento do ano de publicação (o Google manda uma string tipo "2020-05-12" ou só "2020",
        // pegamos os 4 primeiros caracteres se precisar de Integer, ou ajustamos conforme seu BookResponse espera)
        Integer year = null;
        if (info.publishedDate() != null && info.publishedDate().length() >= 4) {
            try {
                year = Integer.parseInt(info.publishedDate().substring(0, 4));
            } catch (NumberFormatException e) {
                year = null;
            }
        }

        return new BookResponse(
                item.id(),           // ID do livro no Google Books
                info.title(),        // Título
                authors,             // Autores formatados
                year,                // Ano extraído
                coverUrl,            // URL da capa
                info.pageCount()     // Número de páginas
        );
    }
}