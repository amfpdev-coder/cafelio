package com.cafelio.api.dto.request;

import com.cafelio.api.model.BookTag;
import com.cafelio.api.model.ReadingStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Set;

public class LibraryBookCreateRequest {

    @NotBlank
    private String openLibraryId;

    @NotBlank
    private String title;

    @NotNull
    private Integer firstPublishYear;

    private String coverUrl;

    @NotNull
    private ReadingStatus status;

    @NotBlank
    private List<String> authors;

    @NotNull
    private Set<BookTag> tags;
}
