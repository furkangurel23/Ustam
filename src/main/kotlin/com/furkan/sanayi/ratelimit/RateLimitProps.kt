package com.furkan.sanayi.ratelimit

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.ratelimit")
data class RateLimitProps(
    var enabled: Boolean = true,
    var perMinute: Int = 30
)
