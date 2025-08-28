package com.furkan.sanayi.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun customOpenAPI(): OpenAPI =
        OpenAPI().info(
            Info().title("Sanayi API")
                .version("v1")
                .description("Servis sağlayıcı arama, puanlama ve yönetim API’leri")
        )
            .components(
                Components().addSecuritySchemes(
                    "basicAuth",
                    SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("basic")
                )
            )
            .addSecurityItem(SecurityRequirement().addList("basicAuth"))
}