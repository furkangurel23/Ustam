package com.furkan.sanayi.security


import com.furkan.sanayi.ratelimit.RateLimitFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.Customizer.withDefaults
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter


@Configuration
@EnableMethodSecurity
class SecurityConfig(
    private val jwtAuthFilter: JwtAuthFilter,
    private val rateLimitFilter: RateLimitFilter
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    /*
    * @Bean ile enjekte edilebilir hale getiriyoruz.
    * Aksi taktirde autowired ile authenticationManager'i iject edememezsin.
    * */
    @Bean
    fun authenticationManager(cfg: AuthenticationConfiguration): AuthenticationManager = cfg.authenticationManager

    /*
    *
    * Stateles API -> Sunucu oturum(session) tutmez; her istek kimligini jwt gibi kendi tasir.
    * Artilari:
    *   - Yatay olcekleme kolay (server ustunde kimin login oldguu bilgisi yok
    *   - sticky session ihtiyaci yok
    * Eksileri:
    *   - logout/token iptali gibi isler ek mekanizma ister (blacklist/short TTL)
    * */

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            //Cross-Site Request forgery: Tarayicnin otomatik gonderdigi cookie'ler yuzunden bir sitenin baska siteye senin adina istek atmasi
            .csrf { it.disable() } // stateless API
            //Session yaratma kullanma. SecurityContext'i sessiona koyma.
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests {
                it
                    .requestMatchers("/api/auth/login", "/v3/api-docs/**", "/swagger-ui/**", "/actuator/health/**")
                    .permitAll()
                    .requestMatchers(
                        HttpMethod.GET,
                        "/api/providers/**",
                        "/api/categories/**",
                        "/api/brands/**",
                        "/api/**/ratings/**"
                    ).permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/ratings/rate").permitAll()
                    // Admin
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")
                    .anyRequest().authenticated()
            }
            .exceptionHandling { conf -> conf.authenticationEntryPoint { _, resp, _ -> resp.sendError(401) } }
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)
            .addFilterBefore(rateLimitFilter, JwtAuthFilter::class.java)
            .httpBasic(withDefaults())
        return http.build()
    }
}