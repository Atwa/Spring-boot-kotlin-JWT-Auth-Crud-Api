package com.atwa.remote_ps.user.payload

data class LoginResponse(
    var token: String,
    val id: Long,
    val username: String,
    val shopId: Long?,
    val isAdmin: Boolean
)