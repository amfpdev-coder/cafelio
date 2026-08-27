package com.cafelio.api.client;

import com.cafelio.api.dto.response.OpenLibrarySearchResponse;
import com.cafelio.api.exception.OpenLibraryUnavailableException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class OpenLibraryClient {

    private final RestClient restClient;

    public OpenLibraryClient(RestClient.Builder builder) {
        this.restClient = builder
                .baseUrl("https://openlibrary.org")
                .build();
    }

    public OpenLibrarySearchResponse searchBooks(
            String titulo,
            int page,
            int limit
    ) {
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search.json")
                            .queryParam("q", titulo)
                            .queryParam("page", page)
                            .queryParam("limit", limit)
                            .queryParam(
                                    "fields",
                                    "key,title,author_name,first_publish_year,cover_i"
                            )
                            .build())
                    .retrieve()
                    .body(OpenLibrarySearchResponse.class);

        } catch (RestClientException e) {
            throw new OpenLibraryUnavailableException(
                    "Não foi possível acessar a Open Library."
            );
        }
    }
}