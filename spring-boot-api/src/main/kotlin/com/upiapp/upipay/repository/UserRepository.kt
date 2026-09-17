package com.upiapp.upipay.repository

import com.upiapp.upipay.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long> {

    // ✅ SAFE PHONE SEARCH (MAIN METHOD)
    @Query(
        """
        SELECT u FROM User u 
        WHERE REPLACE(u.phone, '+', '') = REPLACE(:phone, '+', '')
        """
    )
    fun findByPhoneSafe(phone: String): User?

    // ✅ KEEP THIS (IMPORTANT FOR AUTH CONTROLLER)
    fun findByPhone(phone: String): User?

    fun findByBlocked(blocked: Boolean): List<User>
    fun findByUpiId(upiId: String): User?

    @Query(
        "SELECT FUNCTION('DATE', u.createdAt), COUNT(u) " +
        "FROM User u " +
        "GROUP BY FUNCTION('DATE', u.createdAt) " +
        "ORDER BY FUNCTION('DATE', u.createdAt)"
    )
    fun getDailyUserRegistrations(): List<Array<Any>>

    @Query("SELECT COUNT(u) FROM User u")
 
    fun getTotalUsers(): Long
}