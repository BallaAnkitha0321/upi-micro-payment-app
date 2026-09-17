package com.upiapp.upipay.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

@RestController
class TestController {

    @GetMapping("/test")
    fun test(): String {
        return "Backend is working successfully 🚀"
    }

    // 🔥 NEW: Generate BCrypt hash
    @GetMapping("/generate-hash")
    fun generateHash(): String {
        val encoder = BCryptPasswordEncoder()
        return encoder.encode("Moon143")
    }
}