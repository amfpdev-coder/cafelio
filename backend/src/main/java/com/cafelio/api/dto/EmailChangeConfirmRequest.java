package com.cafelio.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record EmailChangeConfirmRequest(

        @NotBlank(message = "Código é obrigatório")
        @Pattern(regexp = "^\\d{6}$", message = "O código tem 6 dígitos")
        String code
) {
}
