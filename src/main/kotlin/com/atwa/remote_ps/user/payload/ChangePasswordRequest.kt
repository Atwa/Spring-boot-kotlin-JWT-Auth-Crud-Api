package com.atwa.remote_ps.user.payload

data class ChangePasswordRequest(val oldPassword: String, val newPassword: String)