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
        Integer coverId
) {
}
