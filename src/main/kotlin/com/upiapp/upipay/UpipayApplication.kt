package com.upiapp.upipay

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class UpipayApplication

fun main(args: Array<String>) {
    runApplication<UpipayApplication>(*args)
}