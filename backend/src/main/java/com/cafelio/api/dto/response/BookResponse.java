package com.cafelio.api.dto.response;

import java.util.List;

// Classe que entrega os campos ao FrontEnd
public record BookResponse(
        String openLibraryId,
        String title,
        List<String> authors,
        Integer firstPublishYear,
        String coverUrl
) {
}
