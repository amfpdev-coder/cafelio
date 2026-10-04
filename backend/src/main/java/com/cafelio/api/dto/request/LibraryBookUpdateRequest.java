package com.cafelio.api.dto.request;

import com.cafelio.api.model.BookTag;
import com.cafelio.api.model.ReadingStatus;

import java.util.Set;

public record LibraryBookUpdateRequest(
        ReadingStatus status,
        Set<BookTag> tags
) {
}
