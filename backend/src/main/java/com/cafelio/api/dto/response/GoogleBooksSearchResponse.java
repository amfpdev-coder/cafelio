package com.cafelio.api.dto.response;

import java.util.List;

public record GoogleBooksSearchResponse(
        Integer totalItems,
        List<GoogleBookItem> items
) {
        public record GoogleBookItem(
                String id,
                VolumeInfo volumeInfo
        ) {}

        public record VolumeInfo(
                String title,
                String language,
                List<String> authors,
                String publishedDate,
                Integer pageCount,
                ImageLinks imageLinks
        ) {}

        public record ImageLinks(
                String thumbnail
        ) {}
}