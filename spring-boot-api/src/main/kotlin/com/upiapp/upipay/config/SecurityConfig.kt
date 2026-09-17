package com.upiapp.upipay.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
class SecurityConfig(
    private val jwtAuthFilter: JwtAuthFilter
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {

        http
            .csrf { it.disable() }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }

            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }

            .authorizeHttpRequests { auth ->

                auth

                    // ✅ PUBLIC APIs (NO TOKEN REQUIRED)
                    .requestMatchers(
                        "/api/auth/**",
                        "/api/merchant/login",
                        "/api/user/register",
                        "/api/admin/login",
                        "/test" 
                    ).permitAll()

                    // 🔥 FIX: ALLOW TRANSACTION APIs
                    .requestMatchers(
                        "/api/transactions/**"
                    ).permitAll()

                    // 🔒 ALL OTHER APIs REQUIRE TOKEN
                    .anyRequest().authenticated()
            }

            // ✅ JWT FILTER
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
}