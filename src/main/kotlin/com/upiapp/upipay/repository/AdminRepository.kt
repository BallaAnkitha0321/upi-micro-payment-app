package com.upiapp.upipay.repository

import com.upiapp.upipay.entity.Admin
import org.springframework.data.jpa.repository.JpaRepository

interface AdminRepository : JpaRepository<Admin, Long> {
    fun findByEmail(email: String): Admin?
}