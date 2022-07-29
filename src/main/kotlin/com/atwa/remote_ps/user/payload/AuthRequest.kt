package com.atwa.remote_ps.user.payload

import javax.validation.constraints.Size

data class AuthRequest(
    @field:Size(min = 3, max = 20)
    val username: String,
    @field:Size(min = 8, max = 40)
    val password: String,
)