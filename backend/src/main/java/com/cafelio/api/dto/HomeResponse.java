package com.cafelio.api.dto;

import com.cafelio.api.dto.response.LibraryBookResponse;

import java.util.List;

public record HomeResponse(
        int ano,
        long paginasNoAno,
        long paginasNaSemana,
        List<LibraryBookResponse> lendoAgora
) {
}
