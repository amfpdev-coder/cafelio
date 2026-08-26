package com.cafelio.api.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OpenApiConfigTests {

    @Test
    void configuresApiMetadata() {
        var openApi = new OpenApiConfig().cafelioOpenApi();

        assertEquals("Cafélio API", openApi.getInfo().getTitle());
        assertEquals("Documentação da API do Cafélio", openApi.getInfo().getDescription());
        assertEquals("v1", openApi.getInfo().getVersion());
    }
}
