package com.furkan.sanayi.security.recaptcha

interface RecaptchaVerifier {

    fun verifyOrThrow(token: String?, remoteIp: String?)
}