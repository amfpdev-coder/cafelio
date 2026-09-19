package com.cafelio.api.dto.request;

import com.cafelio.api.model.BookTag;
import com.cafelio.api.model.ReadingStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Set;

public record LibraryBookCreateRequest (
        @NotBlank
        String openLibraryId,

        @NotBlank
        String title,

        @NotNull
        Integer firstPublishYear,

        String coverUrl,

        @NotNull
        ReadingStatus status,

        @NotEmpty
        List<String> authors,

        @NotNull
        Set<BookTag> tags
) {


}
