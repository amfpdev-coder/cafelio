package com.cafelio.api.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import com.cafelio.api.exception.GoogleBooksUnavailableException;
import com.cafelio.api.dto.response.GoogleBooksSearchResponse;

@Component
public class GoogleBooksClient {

    private static final Logger log = LoggerFactory.getLogger(GoogleBooksClient.class);

    private final String apiKey;
    private final RestClient restClient;

    public GoogleBooksClient(
            RestClient.Builder builder,
            @Value("${google.books.url}") String baseUrl,
            @Value("${google.books.api-key:}") String apiKey) {
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
                    .uri(uriBuilder -> {
                        var builder = uriBuilder
                                .queryParam("q", "intitle:" + titulo)
                                .queryParam("startIndex", startIndex)
                                .queryParam("maxResults", limit)
                                .queryParam("langRestrict", "pt");

                        if (apiKey != null && !apiKey.isBlank()) {
                            builder.queryParam("key", apiKey);
                        }

                        return builder.build();
                    })
                    .retrieve()
                    .body(GoogleBooksSearchResponse.class);

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("Erro ao acessar a API do Google Books. Status: {}, Resposta: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new GoogleBooksUnavailableException(
                    "Não foi possível acessar a API do Google Books."
            );
        } catch (RestClientException e) {
            log.error("Erro de comunicação com a API do Google Books: {}", e.getMessage(), e);
            throw new GoogleBooksUnavailableException(
                    "Não foi possível acessar a API do Google Books."
            );
        }
    }
}