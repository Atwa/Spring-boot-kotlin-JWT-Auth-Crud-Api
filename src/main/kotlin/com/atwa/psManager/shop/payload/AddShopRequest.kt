package com.atwa.psManager.shop.payload

import java.math.BigDecimal

data class AddShopRequest(
    val userId: Long,
    val name: String,
    val area: String,
    val city: String,
)