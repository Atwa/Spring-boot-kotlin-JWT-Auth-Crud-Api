package com.atwa.remote_ps.user

import com.atwa.remote_ps.user.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByUsername(username: String?): Optional<User>
    fun existsByUsername(username: String): Boolean
}