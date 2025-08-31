package com.furkan.sanayi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching

@SpringBootApplication
@EnableCaching
class SanayiApplication

fun main(args: Array<String>) {
    runApplication<SanayiApplication>(*args)
}
