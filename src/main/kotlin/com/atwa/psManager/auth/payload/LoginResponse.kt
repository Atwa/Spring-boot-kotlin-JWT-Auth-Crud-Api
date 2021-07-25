package com.atwa.psManager.auth.payload

data class LoginResponse(
    var token: String,
    val id: Long,
    val username: String,
    val shopId: Long?,
    val roles: List<String>
)