package com.furkan.sanayi.security.recaptcha

import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate

@Component
class GoogleRecaptchaVerifier(
    private val props: RecaptchaProperties
) : RecaptchaVerifier {

    private val rt = RestTemplate()

    override fun verifyOrThrow(token: String?, remoteIp: String?) {
        if (!props.enabled) return //dev ve local

        val t = token?.trim().orEmpty()
        require(t.isNotEmpty()) { "Missing reCAPTCHA token" }

        val body = LinkedMultiValueMap<String, String>().apply {
            add("secret", props.secret)
            add("response", t)
            if (!remoteIp.isNullOrBlank()) add("remoteIp", remoteIp)
        }

        val req = HttpEntity(body.apply { }, HttpHeaders().apply {
            contentType = MediaType.APPLICATION_FORM_URLENCODED
        })

        val resp: ResponseEntity<Map<*, *>> =
            rt.postForEntity(props.verifyUrl, req, Map::class.java)

        val ok = (resp.body?.get("success") as? Boolean) == true
        if (!ok) {
            val codes = (resp.body?.get("error-codes") as? List<*>)?.joinToString { "," } ?: "unknown"
            throw IllegalStateException("Failed reCAPTCHA: $codes")
        }
    }

}