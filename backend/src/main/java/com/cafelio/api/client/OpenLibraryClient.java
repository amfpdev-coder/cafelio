package com.cafelio.api.client;

import com.cafelio.api.dto.response.OpenLibrarySearchResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class OpenLibraryClient {

    private final RestClient restClient;

    public OpenLibraryClient(RestClient.Builder builder) {
        this.restClient = builder
                .baseUrl("https://openlibrary.org")
                .build();
    }

    public OpenLibrarySearchResponse searchBooks(String titulo, int page, int limit) {
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
    }
}

