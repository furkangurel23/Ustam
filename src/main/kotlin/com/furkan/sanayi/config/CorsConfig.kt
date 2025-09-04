package com.furkan.sanayi.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class CorsConfig(
    @Value("\${app.cors.origins:http://localhost:3000}") private val origins: String
) : WebMvcConfigurer {
    override fun addCorsMappings(reg: CorsRegistry) {
        reg.addMapping("/api/**")
            .allowedOrigins(*origins.split(",").map { it.trim() }.toTypedArray())
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowCredentials(false)
    }
}