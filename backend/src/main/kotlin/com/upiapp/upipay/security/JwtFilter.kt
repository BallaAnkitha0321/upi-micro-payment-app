package com.upiapp.upipay.security

import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException

@Component
class JwtFilter(
    private val jwtService: JwtService
) : OncePerRequestFilter() {

    // ================= SKIP PUBLIC ENDPOINTS =================

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {

        val path = request.servletPath

        return path.startsWith("/api/auth")
                || path.startsWith("/admin/login")
                || path.startsWith("/h2-console")
    }

    // ================= MAIN FILTER =================

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {

        val authHeader = request.getHeader("Authorization")

        // 🚫 No token → continue
        if (authHeader.isNullOrEmpty() || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }

        val token = authHeader.substring(7)

        try {

            // ✅ FIX: Extract PHONE instead of userId
            val phone = jwtService.extractPhone(token)

            // ✅ Set authentication only if not already set
            if (SecurityContextHolder.getContext().authentication == null) {

                val authentication = UsernamePasswordAuthenticationToken(
                    phone, // ✅ PHONE is identity now
                    null,
                    emptyList()
                )

                authentication.details =
                    WebAuthenticationDetailsSource().buildDetails(request)

                SecurityContextHolder.getContext().authentication = authentication
            }

        } catch (e: Exception) {
            println("JWT ERROR: ${e.message}")
        }

        filterChain.doFilter(request, response)
    }
}