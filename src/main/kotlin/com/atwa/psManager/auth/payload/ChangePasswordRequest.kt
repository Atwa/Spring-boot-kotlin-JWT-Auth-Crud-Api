package com.atwa.psManager.auth.payload

data class ChangePasswordRequest(val id: Long, val oldPassword: String, val newPassword: String)