package com.cafelio.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI cafelioOpenApi() {
        return new OpenAPI()
            .info(new Info()
                .title("Cafélio API")
                .description("Documentação da API do Cafélio")
                .version("v1"));
    }
}
