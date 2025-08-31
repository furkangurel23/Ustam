package com.furkan.sanayi.ratelimit

import com.furkan.sanayi.web.ClientIpResolver
import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import io.github.bucket4j.Refill
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

@Component
class RateLimitFilter(
    private val ipResolver: ClientIpResolver
) : OncePerRequestFilter() {
    private val buckets = ConcurrentHashMap<String, Bucket>()
    //kova'nin kapasitesi ve dolum hizini belirliyor. 10 istek/saat ve 3 istek/dakika
    private val perHour = Bandwidth.classic(10, Refill.greedy(10, Duration.ofHours(1)))
    private val perMinute = Bandwidth.classic(3, Refill.greedy(3, Duration.ofMinutes(1)))

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        if (isRatingPost(request)) {
            val key = "rate:${ipResolver.from(request)}"
            val bucket = buckets.computeIfAbsent(key) {
                Bucket.builder().addLimit(perHour).addLimit(perMinute).build()
            }
            if (!bucket.tryConsume(1)) {
                response.status = HttpStatus.TOO_MANY_REQUESTS.value()
                response.contentType = "application/json"
                response.writer.write("""{"error":"Too many requests"}""")
                response.setHeader("Retry-After", "60")
                return
            }
        }
        filterChain.doFilter(request, response)
    }

    private fun isRatingPost(req: HttpServletRequest): Boolean =
        req.method.equals("POST", ignoreCase = true) &&
                req.requestURI.equals("/api/ratings/rate", ignoreCase = true)

}