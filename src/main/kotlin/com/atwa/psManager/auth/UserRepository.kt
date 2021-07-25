package com.atwa.psManager.auth

import com.atwa.psManager.auth.model.User
import org.springframework.context.annotation.Bean
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*
import javax.transaction.Transactional

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByUsername(username: String?): Optional<User>
    fun existsByUsername(username: String): Boolean
}