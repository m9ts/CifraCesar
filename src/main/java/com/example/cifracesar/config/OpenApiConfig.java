package com.example.cifracesar.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Define os metadados exibidos no Swagger UI.
 * Sem este bean o SpringDoc usa o padrão "OpenAPI definition".
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI cifraCesarOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Cifra de César API")
                        .description("API para cifrar, decifrar e quebrar textos com a Cifra de César.")
                        .version("1.0.0"));
    }
}
