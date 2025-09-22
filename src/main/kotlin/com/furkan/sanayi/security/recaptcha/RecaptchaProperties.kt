package com.furkan.sanayi.security.recaptcha

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.recaptcha")
data class RecaptchaProperties(
    val enabled: Boolean = false,
    val secret: String = "",
    val verifyUrl: String = "https://www.google.com/recaptcha/api/siteverify" // v2 checkbox
)