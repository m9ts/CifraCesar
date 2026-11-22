package com.example.cifracesar.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.customizers.OpenApiCustomizer;
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
                        .title("API de Cifra de César")
                        .description("API para cifrar e decifrar mensagens usando a Cifra de César. "
                                + "O alfabeto utilizado é: a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u, v, w, x, y, z. "
                                + "Os espaços devem aparecer nas respostas")
                        .version("1.0.0"));
    }

    @Bean
    public OpenApiCustomizer versaoOpenApi() {
        // O SpringDoc grava 3.0.1; a spec de referencia declara 3.0.0.
        return openApi -> openApi.setOpenapi("3.0.0");
    }
}
