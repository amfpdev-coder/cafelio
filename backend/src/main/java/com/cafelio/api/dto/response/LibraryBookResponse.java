package com.cafelio.api.dto.response;

import com.cafelio.api.model.BookTag;
import com.cafelio.api.model.ReadingStatus;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class LibraryBookResponse {

    private UUID id;

    private String openLibraryId;

    private String title;

    private Integer firstPublishYear;

    private String coverUrl;

    private ReadingStatus status;

    private List<String> authors;

    private Set<BookTag> tags;
}
