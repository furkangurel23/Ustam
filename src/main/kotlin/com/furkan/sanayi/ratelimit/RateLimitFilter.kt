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
    private val ipResolver: ClientIpResolver,
    private val props: RateLimitProps
) : OncePerRequestFilter() {
    private val buckets = ConcurrentHashMap<String, Bucket>()

    /*
    //kova'nin kapasitesi ve dolum hizini belirliyor. 10 istek/saat ve 3 istek/dakika
    private val perHour = Bandwidth.classic(10, Refill.greedy(10, Duration.ofHours(1)))
    private val perMinute = Bandwidth.classic(3, Refill.greedy(3, Duration.ofMinutes(1)))
    */
    override fun doFilterInternal(req: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        if (!props.enabled || !isRatingPost(req)) {
            chain.doFilter(req, response); return
        }
        val ip = ipResolver.from(req)
        val bucket = buckets.computeIfAbsent(ip) {
            val limit = props.perMinute.toLong().coerceAtLeast(1)
            val bandwidth = Bandwidth.classic(limit, Refill.greedy(limit, Duration.ofMinutes(1)))
            Bucket.builder().addLimit(bandwidth).build()
        }
        if (bucket.tryConsume(1)) {
            chain.doFilter(req, response)
        } else {
            response.status = HttpStatus.TOO_MANY_REQUESTS.value()
            response.contentType = "application/json"
            response.setHeader("Retry-After", "60")
            response.writer.write("""{"error":"Too many requests"}""")
        }
    }

    private fun isRatingPost(req: HttpServletRequest): Boolean =
        req.method.equals("POST", ignoreCase = true) &&
                req.requestURI.equals("/api/ratings/rate", ignoreCase = true)

}