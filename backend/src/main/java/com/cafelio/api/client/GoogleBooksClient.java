package com.cafelio.api.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import com.cafelio.api.exception.GoogleBooksUnavailableException;
import com.cafelio.api.dto.response.GoogleBooksSearchResponse;

@Component
public class GoogleBooksClient {

    private final String apiKey;
    private final RestClient restClient;

    public GoogleBooksClient(
            RestClient.Builder builder,
            @Value("${google.books.url}") String baseUrl,
            @Value("${google.books.api-key}") String apiKey) {
        this.restClient = builder
                .baseUrl(baseUrl)
                .build();
        this.apiKey = apiKey;
    }

    public GoogleBooksSearchResponse searchBooks(
            String titulo,
            int page,
            int limit
    ) {
        int startIndex = (page - 1) * limit;
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("q", "intitle:" + titulo)
                            .queryParam("startIndex", startIndex)
                            .queryParam("maxResults", limit)
                            .queryParam("key", apiKey)
                            .build())
                    .retrieve()
                    .body(GoogleBooksSearchResponse.class);

        } catch (RestClientException e) {
            throw new GoogleBooksUnavailableException(
                    "Não foi possível acessar a API do Google Books."
            );
        }
    }
}