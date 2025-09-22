package com.furkan.sanayi

import com.furkan.sanayi.ratelimit.RateLimitProps
import com.furkan.sanayi.security.recaptcha.RecaptchaProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching

@SpringBootApplication
@EnableCaching
@EnableConfigurationProperties(value = [RecaptchaProperties::class, RateLimitProps::class])
class SanayiApplication

fun main(args: Array<String>) {
    runApplication<SanayiApplication>(*args)
}
