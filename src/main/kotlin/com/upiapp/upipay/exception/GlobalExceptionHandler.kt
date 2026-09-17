package com.upiapp.upipay.exception

import com.upiapp.upipay.exception.ResourceNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import java.time.LocalDateTime

@ControllerAdvice
class GlobalExceptionHandler {

    // ================= RESOURCE NOT FOUND =================
    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFound(ex: ResourceNotFoundException): ResponseEntity<ErrorResponse> {

        val error = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.NOT_FOUND.value(),
            message = ex.message ?: "Resource not found"
        )

        return ResponseEntity(error, HttpStatus.NOT_FOUND)
    }

    // ================= RUNTIME EXCEPTION =================
    @ExceptionHandler(RuntimeException::class)
    fun handleRuntimeException(ex: RuntimeException): ResponseEntity<ErrorResponse> {

        // 🔥 HANDLE BLOCKED ACCOUNT (IMPORTANT FIX)
        if (ex.message == "ACCOUNT_RESTRICTED") {

            val error = ErrorResponse(
                timestamp = LocalDateTime.now(),
                status = HttpStatus.FORBIDDEN.value(),
                message = "ACCOUNT_RESTRICTED"
            )

            return ResponseEntity(error, HttpStatus.FORBIDDEN)
        }

        // 🔥 DEFAULT RUNTIME HANDLING
        val error = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.BAD_REQUEST.value(),
            message = ex.message ?: "Runtime error"
        )

        return ResponseEntity(error, HttpStatus.BAD_REQUEST)
    }

    // ================= GENERIC EXCEPTION =================
    @ExceptionHandler(Exception::class)
    fun handleException(ex: Exception): ResponseEntity<ErrorResponse> {

        val error = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            message = ex.message ?: "Unexpected error"
        )

        return ResponseEntity(error, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}