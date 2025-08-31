package com.furkan.sanayi.web

import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Component

@Component
class ClientIpResolver {
    fun from(req: HttpServletRequest): String {
        /*
        * x-forwarded-for
        *   - Istegind gectigi zincirdeki IP'leri tutan HTTP header. formmati client, proxy1, proxy 2, ...
        *   - Genelde reverse proxy/loader balancer(Nginx, HAProxy, ALB vb.)
        * Listenin ilk IP'si gecek istemci IP'si lmayi hedefler. Sonraki IP'ler aradaki proxy'lerdir.
        *
        * X-Real-IP
        *  - Istemcinin gercek IP'si su demek icin kullanilan tek degerli http header.
        *  - Genelde Nginx gibi proxyler
        *  - Farki XFF liste(zincir), X-Real-IP tek bir ip tasir. XFF standartlasmis de facto bir pratik, X-Real_IP daha basit bir konvasyon.
        *
        * req.remoteADdr(Spring/Servlat: HttpServletRequest.getRemoteAddr()
        *   - Uygulama sunucusuna baglana TCP soketini karsi ucu (peer) IP'si
        *   - Ygulama sunucusu saglar (Tomcat/Jetty/Udertow) header degil, transport katmani biligisir
        *   - Proxy varken reverse proxy varsa remoteAddr cogu zaman proxy'nin IP'si olur veya Nginx container IP'si. Gercek istemci IP'si degildir.
        *
        * */

        val xff = req.getHeader("X-Forwarded-For")
            ?.split(',')
            ?.firstOrNull()
            ?.trim()

        val real = req.getHeader("X-Real-IP")?.trim()

        return xff?.takeUnless { it.isBlank() }
            ?: real?.takeUnless { it.isBlank() }
            ?: req.remoteAddr
    }

}