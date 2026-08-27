package com.cafelio.api.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record OpenLibrarySearchResponse(
        @JsonProperty("numFound")
        Integer totalResults,

        Integer start,

        @JsonProperty("numFoundExact")
        Boolean exactResultCount,

        List<OpenLibraryBookResponse> docs
) {
}