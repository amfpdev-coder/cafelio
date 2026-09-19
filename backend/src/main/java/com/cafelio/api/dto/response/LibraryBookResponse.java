package com.cafelio.api.dto.response;

import com.cafelio.api.model.BookTag;
import com.cafelio.api.model.ReadingStatus;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public record LibraryBookResponse(
        UUID id,

        String openLibraryId,

        String title,

        Integer firstPublishYear,

        String coverUrl,

        ReadingStatus status,

        List<String> authors,

        Set<BookTag> tags

) {


}
