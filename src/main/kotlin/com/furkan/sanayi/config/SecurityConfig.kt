package com.furkan.sanayi.config


import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer.withDefaults
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain


@Configuration
class SecurityConfig {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun userDetailsService(passwordEncoder: PasswordEncoder): UserDetailsService {
        val admin = User.builder()
            .username("admin")
            .password(passwordEncoder.encode("admin123"))
            .roles("ADMIN")
            .build()
        return InMemoryUserDetailsManager(admin)
    }

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() } // stateless API
            .authorizeHttpRequests {
                it
                    // PUBLIC endpoints
                    .requestMatchers(
                        "/",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/actuator/health/**",
                        "/api/categories/**",
                        "/api/brands/**",
                        "/api/providers",
                        "/api/providers/**", // GET & NEAR public
                        "/api/ratings/**"    // POST /api/ratings/rate public kalacaksa burayı public'te bırak
                    ).permitAll()
                    // ADMIN endpoints
                    .requestMatchers("/api/admin/**", "/api/providers/**")
                    .hasRole("ADMIN") // POST /api/providers -> admin
                    // diğer her şey
                    .anyRequest().authenticated()
            }
            .httpBasic(withDefaults())
        return http.build()
    }
}