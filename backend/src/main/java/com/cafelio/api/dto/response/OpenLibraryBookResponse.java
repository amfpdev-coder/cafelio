package com.cafelio.api.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record OpenLibraryBookResponse(
       String key,
       String title,

        @JsonProperty("author_name")
        List<String> authors,

        @JsonProperty("first_publish_year")
        Integer firstPublishYear,

        @JsonProperty("cover_i")
        Integer coverId,

        // A Open Library tem uma contagem por edição; a mediana é o valor
        // típico do livro, e é o que eles recomendam para este caso
        @JsonProperty("number_of_pages_median")
        Integer pages
) {
}
