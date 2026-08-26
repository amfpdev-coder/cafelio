package com.cafelio.api.dto.response;

import java.util.List;

public record BookSearchResponse(
        Integer totalResults,
        Integer page,
        Integer limit,
        List<BookResponse> books
) {
}
