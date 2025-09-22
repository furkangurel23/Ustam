package com.furkan.sanayi.security

interface SecurityFacade {

    fun idOrNull(): Long?
    fun emailOrNull(): String?
    fun isAuthenticated(): Boolean
    fun requireId(): Long = idOrNull() ?: error("User is not authenticated")
}