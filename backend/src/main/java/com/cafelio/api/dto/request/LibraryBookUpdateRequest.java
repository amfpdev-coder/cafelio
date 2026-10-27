package com.cafelio.api.dto.request;

import com.cafelio.api.model.BookTag;
import com.cafelio.api.model.LiteraryGenre;
import com.cafelio.api.model.ReadingStatus;

import java.time.LocalDate;
import java.util.Set;

public record LibraryBookUpdateRequest(
        ReadingStatus status,
        Set<BookTag> tags,
        LocalDate startedAt,
        LocalDate finishedAt,
        String country,
        String authorState,
        LiteraryGenre literaryGenre
) {
}
