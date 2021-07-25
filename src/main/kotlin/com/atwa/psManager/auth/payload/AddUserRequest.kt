package com.atwa.psManager.auth.payload

import com.atwa.psManager.shop.Shop
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

class AddUserRequest(
    var username: @NotBlank @Size(min = 3, max = 20) String,
    val password: @NotBlank @Size(min = 8, max = 40) String,
    val shopId: Long,
)